/**
 * 带并发上限的批量请求执行器
 * 分 chunk 并行执行，单条失败不影响其他任务，全部完成后按入参顺序返回结果
 * @param {Array} items - 待处理项数组
 * @param {Function} task - 单项任务函数，接收 item 返回 Promise
 * @param {number} [concurrency=6] - 并发上限
 * @returns {Promise<Array<{item: *, ok: boolean}>>} 每一项的执行结果
 */
export async function runBatchWithLimit(items, task, concurrency = 6) {
  const results = []
  for (let i = 0; i < items.length; i += concurrency) {
    const chunk = items.slice(i, i + concurrency)
    const settled = await Promise.all(
      chunk.map(item =>
        Promise.resolve()
          .then(() => task(item))
          .then(() => true)
          .catch(() => false)
      )
    )
    settled.forEach((ok, index) => {
      results.push({ item: chunk[index], ok })
    })
  }
  return results
}
