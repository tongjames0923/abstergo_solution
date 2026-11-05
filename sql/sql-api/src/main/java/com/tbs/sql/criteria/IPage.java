package com.tbs.sql.criteria;

/**
 * 分页接口，定义了分页查询的标准方法。
 *
 * <p>该接口定义了分页查询的基本参数，包括偏移量和数据量。
 * 实现此接口的类可以作为分页参数传递给SQL查询引擎。</p>
 *
 * <h3>主要功能</h3>
 * <ul>
 *   <li>获取分页偏移量 {@link #from()}</li>
 *   <li>获取每页数据量 {@link #number()}</li>
 * </ul>
 *
 * <h3>使用场景</h3>
 * <ul>
 *   <li>实现分页查询功能</li>
 *   <li>传递分页参数给SQL查询引擎</li>
 *   <li>控制查询结果的分页显示</li>
 * </ul>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * // 实现分页接口
 * public class UserPage implements IPage {
 *     private long page;
 *     private long pageSize;
 *     
 *     @Override
 *     public long from() {
 *         return (page - 1) * pageSize;
 *     }
 *     
 *     @Override
 *     public long number() {
 *         return pageSize;
 *     }
 * }
 * }</pre>
 *
 * <h3>注意事项</h3>
 * <ul>
 *   <li>偏移量从0开始</li>
 *   <li>数据量应该大于0</li>
 *   <li>确保分页参数合理，避免性能问题</li>
 * </ul>
 *
 * @author abstergo
 * @since 1.0.0
 * @see com.tbs.sql.model.query.QueryPageModel
 */
public interface IPage {
    /**
     * 获取分页偏移量。
     *
     * @return 偏移量，从0开始
     */
    long from();

    /**
     * 获取每页数据量。
     *
     * @return 每页显示的记录数
     */
    long number();
}
