import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;



public class App {
    public static class ForkingSpliterator<T>
            extends Spliterators.AbstractSpliterator<T>
    {
        private Spliterator<T> sourceSpliterator;

        private List<BlockingQueue<T>> queues = new ArrayList<>();

        private boolean sourceDone;

        @SafeVarargs
        private ForkingSpliterator(Stream<T> source, GroupingConsumer<T, ?>... consumers)
        {
            super(Long.MAX_VALUE, 0);

            sourceSpliterator = source.spliterator();

            for (GroupingConsumer<T, ?> fork : consumers)
            {
                LinkedBlockingQueue<T> queue = new LinkedBlockingQueue<>();
                queues.add(queue);
                new Thread(() -> {
                    Map grouping = StreamSupport.stream(new ForkedConsumer(queue), false)
                            .collect(Collectors.groupingBy(fork.classifier));



                    fork.consumer.accept(grouping);
                }).start();
            }
        }

        @Override
        public boolean tryAdvance(Consumer<? super T> action)
        {
            sourceDone = !sourceSpliterator.tryAdvance(t -> queues.forEach(queue -> queue.offer(t)));
            return !sourceDone;
        }

        public static class GroupingConsumer<T, R>
        {
            private final Function<T, R> classifier;
            private final Consumer<Map<R, List<T>>> consumer;

            public GroupingConsumer(Function<T, R> classifier, Consumer<Map<R, List<T>>> consumerGrouping)
            {
                this.classifier = classifier;
                this.consumer = consumerGrouping;
            }
        }

        private class ForkedConsumer extends Spliterators.AbstractSpliterator<T>
        {
            private BlockingQueue<T> queue;

            private ForkedConsumer(BlockingQueue<T> queue)
            {
                super(Long.MAX_VALUE, 0);
                this.queue = queue;
            }

            @Override
            public boolean tryAdvance(Consumer<? super T> action)
            {
                while (queue.peek() == null)
                {
                    if (sourceDone)
                    {
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

    @SafeVarargs
    public static <T> long streamForked(Stream<T> source, ForkingSpliterator.GroupingConsumer<T, ?>... consumers) {
        return StreamSupport.stream(new ForkingSpliterator<>(source, consumers), false).count();
    }

    public static void main(String[] args){

        long l = streamForked(Stream.of(new Thing(1), new Thing(2), new Thing(1), new Thing(3), new Thing(2)),
                new ForkingSpliterator.GroupingConsumer<>(Thing::getId, byId -> System.out.println("ID " + byId.values().size())),
                new ForkingSpliterator.GroupingConsumer<>(Thing::getName, byName -> System.out.println("Name " + byName.values().size())),
                new ForkingSpliterator.GroupingConsumer<>(Thing::getClient, byClient -> System.out.println("Client " + byClient.values().size())));

    }
}
