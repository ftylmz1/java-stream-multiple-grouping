import java.util.ArrayList;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;


public class Common {
    public static class StreamConsumer<T, R> {
        private final Function<Stream<T>, R> processor;
        private final Consumer<R> consumer;

        public StreamConsumer(Function<Stream<T>, R> processor, Consumer<R> consumer) {
            this.processor = processor;
            this.consumer = consumer;
        }

        public void process(Stream<T> source) {
            R result = processor.apply(source);
            if (consumer != null) {
                consumer.accept(result);
            }
        }
    }

    public static class ForkingSpliterator<T> extends Spliterators.AbstractSpliterator<T> {
        private Spliterator<T> sourceSpliterator;

        private List<BlockingQueue<T>> queues = new ArrayList<>();

        private boolean sourceDone;

        @SafeVarargs
        public ForkingSpliterator(Stream<T> source, StreamConsumer<T, ?>... consumers) {
            super(Long.MAX_VALUE, 0);

            sourceSpliterator = source.spliterator();

            for (StreamConsumer<T, ?> fork : consumers) {
                LinkedBlockingQueue<T> queue = new LinkedBlockingQueue<>();
                queues.add(queue);
                new Thread(() -> fork.process(StreamSupport.stream(new ForkedConsumer(queue), false))).start();
            }
        }

        @Override
        public boolean tryAdvance(Consumer<? super T> action) {
            sourceDone = !sourceSpliterator.tryAdvance(t -> queues.forEach(queue -> queue.offer(t)));
            return !sourceDone;
        }

        private class ForkedConsumer extends Spliterators.AbstractSpliterator<T> {
            private BlockingQueue<T> queue;

            private ForkedConsumer(BlockingQueue<T> queue) {
                super(Long.MAX_VALUE, 0);
                this.queue = queue;
            }

            @Override
            public boolean tryAdvance(Consumer<? super T> action) {
                while (queue.peek() == null) {
                    if (sourceDone) {
                        // element is null, and there won't be no more, so "terminate" this sub stream
                        return false;
                    }
                }

                // push to consumer pipeline
                action.accept(queue.poll());

                return true;
            }
        }
    }
}

