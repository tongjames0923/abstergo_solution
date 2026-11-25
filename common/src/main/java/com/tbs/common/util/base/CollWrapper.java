package com.tbs.common.util.base;

import com.tbs.common.constant.CommonI18nConstant;
import com.tbs.common.data.IFeatureWrapper;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.error.ErrorLevelEnum;
import com.tbs.common.i18n.I18nUtil;

import java.util.*;
import java.util.function.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * 集合包装器类，提供链式调用API以增强集合操作的易用性
 * <p>
 * 核心特性： 1. 支持重复消费（原生Stream仅能单次遍历） 2. 全链路链式调用，终止操作后仍可继续调用方法 3. 扩展分区、快捷取值等常用集合操作
 * </p>
 *
 * @param <T> 包装元素的类型
 * @author abstergo
 */
public class CollWrapper<T> implements IFeatureWrapper<Collection<T>> {
    // 流供应器：每次调用生成新的Stream实例，实现可重复消费
    private final Supplier<Stream<T>> streamSupplier;

    // 源头数据缓存（仅当通过Stream构造时物化缓存）
    private final List<T> cachedList;

    // ==================== 静态工厂方法 ====================

    /**
     * 创建空的集合包装器
     */
    public static <T> CollWrapper<T> empty() {
        return new CollWrapper<>(Stream::empty, null);
    }

    /**
     * 基于集合创建包装器（惰性求值，不会立即加载全部数据）
     *
     * @param collection 源集合，为null时返回空包装器
     */
    public static <T> CollWrapper<T> of(Collection<T> collection) {
        if (collection == null || collection.isEmpty()) {
            return empty();
        }
        return new CollWrapper<>(collection::stream, null);
    }

    /**
     * 基于数组创建包装器（惰性求值）
     *
     * @param array 源数组，为null时返回空包装器
     */
    @SafeVarargs
    @SuppressWarnings("varargs")
    public static <T> CollWrapper<T> of(T... array) {
        if (array == null || array.length == 0) {
            return empty();
        }
        return new CollWrapper<>(() -> Arrays.stream(array), null);
    }

    public static <T> CollWrapper<T> of(Iterable<T> array) {
        if (array == null || !array.iterator().hasNext()) {
            return empty();
        }
        return new CollWrapper<>(() -> StreamSupport.stream(array.spliterator(), false), null);
    }


    /**
     * 基于Stream创建包装器（会立即物化缓存Stream数据，失去惰性特性）
     * <p>注意：传入的Stream会被完整消费并缓存到内存，以支持重复遍历</p>
     *
     * @param stream 源流，为null时返回空包装器
     */
    public static <T> CollWrapper<T> of(Stream<T> stream) {
        if (stream == null) {
            return empty();
        }
        List<T> data = stream.collect(Collectors.toList());
        return new CollWrapper<>(data::stream, data);
    }

    // ==================== 构造函数 ====================

    /**
     * 内部构造函数：传入流供应器与可选缓存数据
     *
     * @param streamSupplier 流供应器
     * @param cachedList     缓存数据，无可传null
     */
    private CollWrapper(Supplier<Stream<T>> streamSupplier, List<T> cachedList) {
        this.streamSupplier = streamSupplier;
        this.cachedList = cachedList;
    }

    /**
     * 构造函数，创建包含指定流的集合包装器
     *
     * @param stream 要包装的流，如果为null则使用空流
     * @deprecated 推荐使用 {@link #of(Stream)} 静态工厂，语义更清晰
     */
    @Deprecated
    public CollWrapper(Stream<T> stream) {
        if (stream == null) {
            this.streamSupplier = Stream::empty;
            this.cachedList = null;
        } else {
            List<T> data = stream.collect(Collectors.toList());
            this.streamSupplier = data::stream;
            this.cachedList = data;
        }
    }

    // ==================== 内部工具方法 ====================

    /**
     * 获取一个新的 Stream 实例（每次调用返回全新的Stream，支持重复消费）
     */
    private Stream<T> getStream() {
        return streamSupplier.get();
    }

    // ==================== 接口实现 ====================

    @Override
    public Collection<T> getOriginal() {
        return toList();
    }

    @Override
    public void setOriginal(Collection<T> orginal) {
        if (orginal == null) {
            AssertUtil.throwError(() ->
                new IllegalArgumentException(I18nUtil.get(CommonI18nConstant.COMMON_ORIGINAL_REQUIRED)));
        }
        // CollWrapper 设计为不可变对象，不支持修改原始数据
        throw new UnsupportedOperationException("CollWrapper is immutable, cannot set original collection");
    }

    // ==================== 中间操作（返回新包装器，惰性执行） ====================

    /**
     * 过滤流中的元素，返回新包装器
     */
    public CollWrapper<T> filter(Predicate<? super T> predicate) {
        return new CollWrapper<>(() -> getStream().filter(predicate), null);
    }

    /**
     * 元素类型转换，返回新包装器
     */
    public <R> CollWrapper<R> map(Function<? super T, ? extends R> mapper) {
        return new CollWrapper<>(() -> getStream().map(mapper), null);
    }

    /**
     * 扁平化映射，支持转换元素类型（修复原方法泛型受限问题）
     */
    public <R> CollWrapper<R> flatMap(Function<? super T, ? extends Stream<? extends R>> mapper) {
        return new CollWrapper<>(() -> getStream().flatMap(mapper), null);
    }

    /**
     * 遍历元素并执行操作，返回当前包装器（支持链式调用）
     * <p>注意：此方法会完整消费一次流，后续操作将重新执行全链路计算</p>
     */
    public CollWrapper<T> forEach(Consumer<? super T> action) {
        getStream().forEach(action);
        return this;
    }

    /**
     * 元素旁路操作（不消费流，仅在元素被消费时执行副作用）
     */
    public CollWrapper<T> peek(Consumer<? super T> action) {
        return new CollWrapper<>(() -> getStream().peek(action), null);
    }

    /**
     * 限制最大元素数量
     */
    public CollWrapper<T> limit(long maxSize) {
        return new CollWrapper<>(() -> getStream().limit(maxSize), null);
    }

    /**
     * 跳过前n个元素
     */
    public CollWrapper<T> skip(long n) {
        return new CollWrapper<>(() -> getStream().skip(n), null);
    }

    /**
     * 按指定比较器排序
     */
    public CollWrapper<T> sorted(Comparator<? super T> comparator) {
        return new CollWrapper<>(() -> getStream().sorted(comparator), null);
    }

    /**
     * 自然排序（元素需实现Comparable接口）
     */
    @SuppressWarnings("unchecked")
    public CollWrapper<T> sorted() {
        return new CollWrapper<>(() -> getStream().sorted((Comparator<? super T>)Comparator.naturalOrder()), null);
    }

    /**
     * 去重
     */
    public CollWrapper<T> distinct() {
        return new CollWrapper<>(() -> getStream().distinct(), null);
    }

    /**
     * 转为并行流
     */
    public CollWrapper<T> parallel() {
        return new CollWrapper<>(() -> getStream().parallel(), null);
    }

    /**
     * 转为顺序流
     */
    public CollWrapper<T> sequential() {
        return new CollWrapper<>(() -> getStream().sequential(), null);
    }

    /**
     * 转为无序流
     */
    public CollWrapper<T> unordered() {
        return new CollWrapper<>(() -> getStream().unordered(), null);
    }

    // ==================== 查找与匹配（终止操作） ====================

    /**
     * 查找第一个元素
     */
    public Optional<T> findFirst() {
        return getStream().findFirst();
    }

    /**
     * 获取第一个元素，为空则返回null
     */
    public T findFirstOrNull() {
        return findFirst().orElse(null);
    }

    /**
     * 查找任意元素（并行流下性能更优）
     */
    public Optional<T> findAny() {
        return getStream().findAny();
    }

    /**
     * 是否存在任意元素匹配谓词
     */
    public boolean anyMatch(Predicate<? super T> predicate) {
        return getStream().anyMatch(predicate);
    }

    /**
     * 是否所有元素都匹配谓词
     */
    public boolean allMatch(Predicate<? super T> predicate) {
        return getStream().allMatch(predicate);
    }

    /**
     * 是否没有元素匹配谓词
     */
    public boolean noneMatch(Predicate<? super T> predicate) {
        return getStream().noneMatch(predicate);
    }

    /**
     * 元素总数
     */
    public long count() {
        return getStream().count();
    }

    /**
     * 是否为空
     */
    public boolean isEmpty() {
        return !getStream().findAny().isPresent();
    }

    /**
     * 是否非空
     */
    public boolean isNotEmpty() {
        return getStream().findAny().isPresent();
    }

    // ==================== 收集操作（终止操作） ====================

    /**
     * 收集为可变List
     */
    public List<T> toList() {
        return getStream().collect(Collectors.toList());
    }

    /**
     * 收集为不可变List
     */
    public List<T> toUnmodifiableList() {
        return Collections.unmodifiableList(getStream().collect(Collectors.toList()));
    }

    /**
     * 收集为可变Set
     */
    public Set<T> toSet() {
        return getStream().collect(Collectors.toSet());
    }

    /**
     * 收集为不可变Set（移除冗余拷贝）
     */
    public Set<T> toUnmodifiableSet() {
        return Collections.unmodifiableSet(getStream().collect(Collectors.toSet()));
    }

    /**
     * 收集为Object数组
     */
    public Object[] toArray() {
        return getStream().toArray();
    }

    /**
     * 收集为指定类型数组
     */
    public <A> A[] toArray(IntFunction<A[]> generator) {
        return getStream().toArray(generator);
    }

    /**
     * 自定义收集转换
     */
    public <R> R to(Function<Stream<T>, R> mapper) {
        return mapper.apply(getStream());
    }

    /**
     * 收集为Map（键重复时抛出IllegalStateException）
     */
    public <K, V> Map<K, V> toMap(Function<? super T, ? extends K> keyMapper,
        Function<? super T, ? extends V> valueMapper) {
        return getStream().collect(Collectors.toMap(keyMapper, valueMapper));
    }

    /**
     * 收集为Map，自定义重复键合并策略
     */
    public <K, V> Map<K, V> toMap(Function<? super T, ? extends K> keyMapper,
        Function<? super T, ? extends V> valueMapper, BinaryOperator<V> mergeFunction) {
        return getStream().collect(Collectors.toMap(keyMapper, valueMapper, mergeFunction));
    }

    /**
     * 收集为指定类型Map，自定义重复键合并策略
     *
     * @param mapFactory Map创建工厂，例如 HashMap::new、LinkedHashMap::new
     */
    public <K, V, M extends Map<K, V>> M toMap(Function<? super T, ? extends K> keyMapper,
        Function<? super T, ? extends V> valueMapper, BinaryOperator<V> mergeFunction, Supplier<M> mapFactory) {
        return getStream().collect(Collectors.toMap(keyMapper, valueMapper, mergeFunction, mapFactory));
    }

    // ==================== 聚合操作 ====================

    /**
     * 获取最大元素
     */
    public Optional<T> max(Comparator<? super T> comparator) {
        return getStream().max(comparator);
    }

    /**
     * 获取最小元素
     */
    public Optional<T> min(Comparator<? super T> comparator) {
        return getStream().min(comparator);
    }

    /**
     * 无初始值归约
     */
    public Optional<T> reduce(BinaryOperator<T> accumulator) {
        return getStream().reduce(accumulator);
    }

    /**
     * 带初始值归约
     */
    public T reduce(T identity, BinaryOperator<T> accumulator) {
        return getStream().reduce(identity, accumulator);
    }

    /**
     * 三参数归约（支持并行流）
     */
    public <U> U reduce(U identity, BiFunction<U, ? super T, U> accumulator, BinaryOperator<U> combiner) {
        return getStream().reduce(identity, accumulator, combiner);
    }

    // ==================== 扩展操作 ====================

    /**
     * 将集合按指定大小分片
     *
     * @param size 每组元素数量，必须为正整数
     * @return 分片后的包装器嵌套结构
     */
    public CollWrapper<CollWrapper<T>> partition(int size) {
        if (isEmpty()) {
            return empty();
        }

        AssertUtil.toAssert(size <= 0).i18n(CommonI18nConstant.COLL_SPLIT_SIZE_POSITIVE)
            .errorLevel(ErrorLevelEnum.BUSINESS_LOGIC_ERROR).throwIt();

        List<T> dataList = this.toList();
        int totalSize = dataList.size();
        // 修正命名：原partitionSize易歧义，改为分组总数
        int groupCount = (int)Math.ceil((double)totalSize / size);
        List<CollWrapper<T>> result = new ArrayList<>(groupCount);

        for (int i = 0; i < groupCount; i++) {
            int start = i * size;
            int end = Math.min(start + size, totalSize);
            result.add(CollWrapper.of(dataList.subList(start, end).stream()));
        }
        return CollWrapper.of(result.stream());
    }
}
