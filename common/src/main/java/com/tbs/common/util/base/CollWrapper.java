package com.tbs.common.util.base;

import com.tbs.common.data.IFeatureWrapper;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.error.ErrorLevelEnum;

import java.util.*;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 集合包装器类，提供链式调用API以增强集合操作的易用性
 *
 * @param <T> 包装元素的类型
 * @author abstergo
 */
public class CollWrapper<T> implements IFeatureWrapper<Collection<T>> {
    Stream<T> stream;

    /**
     * 构造函数，创建包含指定流的集合包装器
     *
     * @param stream 要包装的流，如果为null则使用空流
     */
    public CollWrapper(Stream<T> stream) {
        this.stream = stream == null ? Stream.empty() : stream;

    }

    @Override
    public Collection<T> getOrginal() {
        return stream.collect(Collectors.toList());
    }

    @Override
    public void setOrginal(Collection<T> orginal) {
        this.stream = orginal == null ? Stream.empty() : orginal.stream();
    }

    /**
     * 过滤流中的元素，返回一个新的包装器
     *
     * @param predicate 用于测试元素的谓词函数
     * @return 包含过滤后元素的新集合包装器
     */
    public CollWrapper<T> filter(java.util.function.Predicate<T> predicate) {
        return new CollWrapper<>(stream.filter(predicate));
    }

    /**
     * 将流中的每个元素转换为另一个值，返回一个新的包装器
     *
     * @param mapper 用于转换元素的函数
     * @return 包含转换后元素的新集合包装器
     */
    public <R> CollWrapper<R> map(java.util.function.Function<T, R> mapper) {
        return new CollWrapper<>(stream.map(mapper));
    }

    public CollWrapper<T> forEach(java.util.function.Consumer<T> action) {
        stream.forEach(action);
        return this;
    }

    /**
     * 将流中的每个元素转换为流，然后将这些流扁平化为一个流，返回一个新的包装器
     *
     * @param mapper 用于将元素转换为流的函数
     * @return 包含扁平化后元素的新集合包装器
     */
    public CollWrapper<T> flatMap(java.util.function.Function<T, Stream<T>> mapper) {
        return new CollWrapper<>(stream.flatMap(mapper));
    }

    /**
     * 对流中的每个元素执行操作，返回一个新的包装器
     *
     * @param action 对每个元素执行的操作
     * @return 包含相同元素的新集合包装器（但带有副作用操作）
     */
    public CollWrapper<T> peek(java.util.function.Consumer<T> action) {
        return new CollWrapper<>(stream.peek(action));
    }

    /**
     * 限制流中的元素数量，返回一个新的包装器
     *
     * @param maxSize 最大元素数量
     * @return 包含限制后元素的新集合包装器
     */
    public CollWrapper<T> limit(long maxSize) {
        return new CollWrapper<>(stream.limit(maxSize));
    }

    /**
     * 查找流中的第一个元素
     *
     * @return 包含第一个元素的Optional对象，如果流为空则返回空Optional
     */
    public Optional<T> findFirst() {
        return stream.findFirst();
    }

    /**
     * 获取流中的第一个元素，如果流为空则返回null
     *
     * @return 流中的第一个元素，如果流为空则返回null
     */
    public T findFirstOrNull() {
        return findFirst().orElse(null);
    }

    /**
     * 查找流中的任意元素
     *
     * @return 包含任意元素的Optional对象，如果流为空则返回空Optional
     */
    public Optional<T> findAny() {
        return stream.findAny();
    }

    /**
     * 跳过流中的前n个元素，返回一个新的包装器
     *
     * @param n 要跳过的元素数量
     * @return 包含跳过后剩余元素的新集合包装器
     */
    public CollWrapper<T> skip(long n) {
        return new CollWrapper<>(stream.skip(n));
    }

    /**
     * 对流中的元素进行排序，返回一个新的包装器
     *
     * @param comparator 用于比较元素的比较器
     * @return 包含排序后元素的新集合包装器
     */
    public CollWrapper<T> sorted(java.util.Comparator<T> comparator) {
        return new CollWrapper<>(stream.sorted(comparator));
    }

    /**
     * 对流中的元素进行自然排序，返回一个新的包装器
     *
     * @return 包含自然排序后元素的新集合包装器
     */
    public CollWrapper<T> sorted() {
        return new CollWrapper<>(stream.sorted());
    }

    /**
     * 去除流中的重复元素，返回一个新的包装器
     *
     * @return 包含去重后元素的新集合包装器
     */
    public CollWrapper<T> distinct() {
        return new CollWrapper<>(stream.distinct());
    }

    /**
     * 将流设置为并行流，返回一个新的包装器
     *
     * @return 包含并行流元素的新集合包装器
     */
    public CollWrapper<T> parallel() {
        return new CollWrapper<>(stream.parallel());
    }

    /**
     * 将流设置为顺序流，返回一个新的包装器
     *
     * @return 包含顺序流元素的新集合包装器
     */
    public CollWrapper<T> sequential() {
        return new CollWrapper<>(stream.sequential());
    }

    /**
     * 将流设置为无序流，返回一个新的包装器
     *
     * @return 包含无序流元素的新集合包装器
     */
    public CollWrapper<T> unordered() {
        return new CollWrapper<>(stream.unordered());
    }

    /**
     * 检查流中是否有任何元素匹配给定的谓词
     *
     * @param predicate 用于测试元素的谓词函数
     * @return 如果有任何元素匹配谓词则返回true，否则返回false
     */
    public boolean anyMatch(java.util.function.Predicate<T> predicate) {
        return stream.anyMatch(predicate);
    }

    /**
     * 检查流中是否所有元素都匹配给定的谓词
     *
     * @param predicate 用于测试元素的谓词函数
     * @return 如果所有元素都匹配谓词则返回true，否则返回false
     */
    public boolean allMatch(java.util.function.Predicate<T> predicate) {
        return stream.allMatch(predicate);
    }

    /**
     * 检查流中是否没有任何元素匹配给定的谓词
     *
     * @param predicate 用于测试元素的谓词函数
     * @return 如果没有任何元素匹配谓词则返回true，否则返回false
     */
    public boolean noneMatch(java.util.function.Predicate<T> predicate) {
        return stream.noneMatch(predicate);
    }

    /**
     * 计算流中元素的数量
     *
     * @return 流中元素的数量
     */
    public long count() {
        return stream.count();
    }

    /**
     * 将流中的元素收集到列表中
     *
     * @return 包含流中所有元素的列表
     */
    public List<T> toList() {
        return stream.collect(Collectors.toList());
    }

    /**
     * 将流中的元素收集到不可变列表中
     *
     * @return 包含流中所有元素的不可变列表
     */
    public List<T> toUnmodifiableList() {
        return Collections.unmodifiableList(stream.collect(Collectors.toList()));
    }

    /**
     * 使用给定的收集器函数将流转换为其他类型
     *
     * @param mapper 用于转换流的函数
     * @param <R>    转换后的结果类型
     * @return 转换后的结果
     */
    public <R> R to(java.util.function.Function<Stream<T>, R> mapper) {
        return mapper.apply(stream);
    }

    /**
     * 将流中的元素收集到Map中
     *
     * @param keyMapper   用于生成键的函数
     * @param valueMapper 用于生成值的函数
     * @param <K>         Map中键的类型
     * @param <V>         Map中值的类型
     * @return 包含流中所有元素映射的Map
     */
    public <K, V> Map<K, V> toMap(java.util.function.Function<T, K> keyMapper,
        java.util.function.Function<T, V> valueMapper) {
        return stream.collect(Collectors.toMap(keyMapper, valueMapper));
    }

    /**
     * 将流中的元素收集到Map中，使用合并函数处理重复键
     *
     * @param keyMapper     用于生成键的函数
     * @param valueMapper   用于生成值的函数
     * @param mergeFunction 用于合并具有相同键的值的函数
     * @param <K>           Map中键的类型
     * @param <V>           Map中值的类型
     * @return 包含流中所有元素映射的Map
     */
    public <K, V> Map<K, V> toMap(java.util.function.Function<T, K> keyMapper,
        java.util.function.Function<T, V> valueMapper, java.util.function.BinaryOperator<V> mergeFunction) {
        return stream.collect(Collectors.toMap(keyMapper, valueMapper, mergeFunction));
    }

    /**
     * 将流中的元素收集到Map中，使用提供的Map工厂
     *
     * @param keyMapper   用于生成键的函数
     * @param valueMapper 用于生成值的函数
     * @param mapFactory  用于创建Map的工厂函数
     * @param <K>         Map中键的类型
     * @param <V>         Map中值的类型
     * @param <M>         Map的具体类型
     * @return 包含流中所有元素映射的Map
     */
    public <K, V, M extends Map<K, V>> M toMap(java.util.function.Function<T, K> keyMapper,
        java.util.function.Function<T, V> valueMapper, java.util.function.Supplier<M> mapFactory) {
        return stream.collect(Collectors.toMap(keyMapper, valueMapper, (v1, v2) -> v1, mapFactory));
    }

    /**
     * 将流中的元素收集到Set中
     *
     * @return 包含流中所有元素的Set
     */
    public Set<T> toSet() {
        return stream.collect(Collectors.toSet());
    }

    /**
     * 将流中的元素收集到不可变Set中
     *
     * @return 包含流中所有元素的不可变Set
     */
    public Set<T> toUnmodifiableSet() {
        return Collections.unmodifiableSet(new HashSet<>(stream.collect(Collectors.toSet())));
    }

    /**
     * 将流中的元素收集到数组中
     *
     * @return 包含流中所有元素的数组
     */
    public Object[] toArray() {
        return stream.toArray();
    }

    /**
     * 将流中的元素收集到指定类型的数组中
     *
     * @param generator 用于创建数组的函数
     * @param <A>       数组元素的类型
     * @return 包含流中所有元素的数组
     */
    public <A> A[] toArray(java.util.function.IntFunction<A[]> generator) {
        return stream.toArray(generator);
    }

    /**
     * 获取流中的最大元素
     *
     * @param comparator 用于比较元素的比较器
     * @return 包含最大元素的Optional对象，如果流为空则返回空Optional
     */
    public Optional<T> max(Comparator<T> comparator) {
        return stream.max(comparator);
    }

    /**
     * 获取流中的最小元素
     *
     * @param comparator 用于比较元素的比较器
     * @return 包含最小元素的Optional对象，如果流为空则返回空Optional
     */
    public Optional<T> min(Comparator<T> comparator) {
        return stream.min(comparator);
    }

    /**
     * 对流中的元素执行归约操作
     *
     * @param accumulator 用于组合元素的函数
     * @return 归约后的结果，如果没有元素则返回Optional.empty()
     */
    public Optional<T> reduce(BinaryOperator<T> accumulator) {
        return stream.reduce(accumulator);
    }

    /**
     * 对流中的元素执行归约操作
     *
     * @param identity    初始值
     * @param accumulator 用于组合元素的函数
     * @return 归约后的结果
     */
    public T reduce(T identity, BinaryOperator<T> accumulator) {
        return stream.reduce(identity, accumulator);
    }

    /**
     * 对流中的元素执行归约操作
     *
     * @param identity    初始值
     * @param accumulator 用于组合元素的函数
     * @param combiner    用于组合部分结果的函数
     * @param <U>         归约结果的类型
     * @return 归约后的结果
     */
    public <U> U reduce(U identity, java.util.function.BiFunction<U, ? super T, U> accumulator,
        BinaryOperator<U> combiner) {
        return stream.reduce(identity, accumulator, combiner);
    }

    /**
     * 检查流是否为空
     *
     * @return 如果流为空则返回true，否则返回false
     */
    public boolean isEmpty() {
        return !stream.findAny().isPresent();
    }

    /**
     * 检查流是否非空
     *
     * @return 如果流非空则返回true，否则返回false
     */
    public boolean isNotEmpty() {
        return stream.findAny().isPresent();
    }

    /**
     * 将集合均分成n个集合
     *
     * @param array
     * @param size
     * @param <T>
     * @return
     */
    public CollWrapper<CollWrapper<T>> partition(int size) {
        if (isEmpty()) {
            return new CollWrapper<>(null);
        }

        AssertUtil.toAssert(size <= 0).message("分割大小必须为正数").errorLevel(ErrorLevelEnum.BUSINESS_LOGIC_ERROR)
            .throwIt();
        int partitionSize = (int)Math.ceil((double)count() / size);
        List<CollWrapper<T>> result = new ArrayList<>(partitionSize);

        for (int i = 0; i < partitionSize; i++) {
            int start = i * size;
            int end = Math.toIntExact(Math.min(start + size, count()));
            result.add(new CollWrapper<T>(stream.skip(start).limit(end)));
        }
        return new CollWrapper<>(result.stream());
    }
}
