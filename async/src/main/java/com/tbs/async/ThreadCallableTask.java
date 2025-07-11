package com.tbs.async;

import com.tbs.async.util.ThreadUtil;
import com.tbs.common.function.SBiConsumer;
import com.tbs.common.util.CollUtil;
import kotlin.Pair;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * <p>
 * 一个泛型类，用于在异步线程中执行带有参数的消费者任务。
 * </p>
 *
 * <p>
 * 该类实现了 {@link Serializable} 接口，支持序列化和反序列化。 它封装了一个对象 {@code T} 和一个消费者函数式接口 {@link SBiConsumer}，以及一个提供参数的
 * {@link Supplier}。
 * </p>
 *
 * <p>
 * 使用示例：
 * </p>
 *
 * <pre>
 * // 示例：对某个对象 T 执行异步操作
 * MyObject obj = new MyObject();
 * ThreadCallableTask<MyObject, String> task = new ThreadCallableTask<>(obj, (o, p) -> o.process(p), () -> "parameter");
 * MyObject result = task.get(); // 获取异步执行后的结果
 * </pre>
 *
 * @param <T> 表示被操作的对象类型
 * @param <P> 表示传递给消费者函数的参数类型
 * @author tongj
 */
@NoArgsConstructor
public class ThreadCallableTask<T, P> implements Serializable {
    /**
     * 序列化版本 UID，用于支持对象序列化。
     */
    private static final long serialVersionUID = 1L;

    /**
     * 被操作的对象，类型为泛型 {@code T}。
     */
    @Setter
    private T object;

    /**
     * 消费者函数，接受两个参数：对象 {@code T} 和 参数 {@code P}。 通常用于定义对对象的操作逻辑。
     */
    @Getter
    @Setter
    private SBiConsumer<T, P> function;

    /**
     * 提供参数的函数式接口，用于生成传递给消费者函数的参数值。
     */
    @Getter
    @Setter
    private Supplier<P> supplier;

    /**
     * 异步执行的结果，使用 {@link CompletableFuture} 包装。
     */
    @Setter
    private CompletableFuture<T> future;

    /**
     * 构造方法，初始化对象、消费者函数和参数提供者。
     *
     * @param object   被操作的对象，类型为 {@code T}
     * @param function 消费者函数，接受对象和参数并执行操作
     * @param supplier 提供参数的函数式接口
     */
    public ThreadCallableTask(T object, SBiConsumer<T, P> function, Supplier<P> supplier) {
        this.object = object;
        this.function = function;
        this.supplier = supplier;
    }

    public static final class Builder<T> {
        private T object;
        private List<Pair<SBiConsumer, Supplier>> applyers = new LinkedList<>();

        public Builder(T object) {
            if (object == null) {
                throw new IllegalArgumentException("object can not be null");
            }
            this.object = object;
        }

        /**
         * 添加一个异步任务
         * @param function
         * @param supplier
         * @param <P>
         * @return
         */

        public <P> Builder<T> run(SBiConsumer<T, P> function, Supplier<P> supplier) {
            applyers.add(new Pair<>(function, supplier));
            return this;
        }

        /**
         * 构建并执行异步任务。
         *
         * <p>
         * 调用 {@link #run(SBiConsumer, Supplier)} 方法添加多个消费者任务。 然后，遍历所有任务，并调用 {@link ThreadUtil#getInstance()}
         * 方法创建一个新的异步任务，并返回一个包含所有任务的列表。
         * </p>
         *
         * @return 包含所有异步任务列表
         */
        public List<ThreadCallableTask> buildAndExecuteAsync() {
            if (CollUtil.isNotEmpty(applyers)) {
                return applyers.stream().map(pair -> {
                    return ThreadUtil.getInstance().asyncCall(object, pair.getFirst(), pair.getSecond());
                }).collect(Collectors.toList());

            }
            return List.of();
        }

        /**
         * 构建并执行异步任务，并等待所有任务完成。
         *
         * <p>
         * 调用 {@link #buildAndExecuteAsync()} 方法构建并执行异步任务。 然后，遍历所有任务，并调用 {@link ThreadCallableTask#get()} 方法等待每个任务完成。
         * </p>
         *
         * @return 构建并执行的异步任务列表
         */
        public T buildAndExecute() {
            List<ThreadCallableTask> tasks = buildAndExecuteAsync();
            for (ThreadCallableTask task : tasks) {
                task.get();
            }
            return object;
        }
    }

    /**
     * 获取异步执行的结果。如果尚未执行，则启动异步任务。
     *
     * <p>
     * 如果当前没有设置 future，则创建一个新的异步任务，调用 {@link #function} 并传入由 {@link #supplier} 提供的参数。 最终返回原始对象 {@code T}。
     * </p>
     *
     * @return 返回异步执行后的对象 {@code T}
     */
    public T get() {
        if (future == null) {
            future = CompletableFuture.supplyAsync(() -> {
                function.accept(object, supplier.get());
                return object;
            });
        }
        return future.join();
    }
}
