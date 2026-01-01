import * as echarts from 'echarts'

/**
 * 初始化响应时间分析图表
 * @param {HTMLElement} chartElement - 图表容器元素
 * @param {Array} chartData - 图表数据
 * @param {Object} options - 图表配置选项
 * @returns {echarts.ECharts} echarts实例
 */
export function initResponseTimeChart(chartElement, chartData, options = {}) {
  if (!chartElement) return null

  // 默认配置
  const defaultOptions = {
    sortType: 'stdDev', // stdDev, avg, max, min, median, count
    sortOrder: 'desc', // desc, asc
    showTop: 15, // 显示前N个
    showAverage: true // 是否显示平均耗时指标
  }

  const config = { ...defaultOptions, ...options }

  // 先销毁已有的图表实例
  const existingChart = echarts.getInstanceByDom(chartElement)
  if (existingChart) {
    existingChart.dispose()
  }

  const chart = echarts.init(chartElement)

  // 根据配置排序和筛选数据
  let sortedData = [...chartData]

  // 排序函数
  const getSortValue = (item, type) => {
    switch (type) {
      case 'stdDev':
        return parseFloat(item.operateTimeStdDev) || 0
      case 'avg':
        const totalTime = parseFloat(item.operateTime) || 0
        const count = parseFloat(item.operateCount) || 0
        return count > 0 ? (totalTime / count) : 0
      case 'max':
        return parseFloat(item.operateTimeMax) || 0
      case 'min':
        return parseFloat(item.operateTimeMin) || 0
      case 'median':
        return parseFloat(item.operateTimeMedian) || 0
      case 'count':
        return parseFloat(item.operateCount) || 0
      default:
        return parseFloat(item.operateTimeStdDev) || 0
    }
  }

  // 根据配置排序
  sortedData.sort((a, b) => {
    const valueA = getSortValue(a, config.sortType)
    const valueB = getSortValue(b, config.sortType)
    return config.sortOrder === 'desc' ? valueB - valueA : valueA - valueB
  })

  // 取前N个
  sortedData = sortedData.slice(0, config.showTop)

  // 为水平条形图交换数据顺序（因为ECharts的y轴是从下到上显示的）
  const yAxisData = sortedData.map(item => item.operateType).reverse()

  // 多系列数据
  const seriesMax = sortedData.map(item => parseFloat(item.operateTimeMax) || 0).reverse()
  const series90Percentile = sortedData.map(item => parseFloat(item.operateTime90Percentile) || 0).reverse()
  const seriesMedian = sortedData.map(item => parseFloat(item.operateTimeMedian) || 0).reverse()
  const seriesMin = sortedData.map(item => parseFloat(item.operateTimeMin) || 0).reverse()
  const seriesAvg = sortedData.map(item => {
    const totalTime = parseFloat(item.operateTime) || 0
    const count = parseFloat(item.operateCount) || 0
    return count > 0 ? (totalTime / count) : 0
  }).reverse()
  const seriesStdDev = sortedData.map(item => parseFloat(item.operateTimeStdDev) || 0).reverse()
  const seriesCount = sortedData.map(item => parseFloat(item.operateCount) || 0).reverse()

  // 计算整体平均值
  const totalAvg = sortedData.length > 0
    ? sortedData.reduce((sum, item) => {
      const totalTime = parseFloat(item.operateTime) || 0
      const count = parseFloat(item.operateCount) || 0
      return sum + (count > 0 ? (totalTime / count) : 0)
    }, 0) / sortedData.length : 0

  // 使用统一的配色方案
  const colors = ['#ff7875', '#ffa940', '#fadb14', '#73d13d', '#40a9ff', '#b37feb', '#ff7875']

  const series = [
    {
      name: '最大响应时间',
      type: 'bar',
      barGap: '0%',
      barWidth: '10%',
      data: seriesMax,
      itemStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
          { offset: 0, color: colors[0] },
          { offset: 1, color: colors[0] + '80' }
        ]),
        borderRadius: [0, 4, 4, 0]
      },
      emphasis: {
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
            { offset: 0, color: colors[0] },
            { offset: 1, color: colors[0] }
          ]),
          shadowBlur: 10,
          shadowOffsetX: 0,
          shadowColor: 'rgba(0, 0, 0, 0.2)'
        }
      },
      animationDelay: function(idx) {
        return idx * 10
      }
    },
    {
      name: '90%分位响应时间',
      type: 'bar',
      barGap: '0%',
      barWidth: '10%',
      data: series90Percentile,
      itemStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
          { offset: 0, color: colors[1] },
          { offset: 1, color: colors[1] + '80' }
        ]),
        borderRadius: [0, 4, 4, 0]
      },
      emphasis: {
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
            { offset: 0, color: colors[1] },
            { offset: 1, color: colors[1] }
          ]),
          shadowBlur: 10,
          shadowOffsetX: 0,
          shadowColor: 'rgba(0, 0, 0, 0.2)'
        }
      },
      animationDelay: function(idx) {
        return idx * 10 + 100
      }
    },
    {
      name: '中位数响应时间',
      type: 'bar',
      barGap: '0%',
      barWidth: '10%',
      data: seriesMedian,
      itemStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
          { offset: 0, color: colors[2] },
          { offset: 1, color: colors[2] + '80' }
        ]),
        borderRadius: [0, 4, 4, 0]
      },
      emphasis: {
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
            { offset: 0, color: colors[2] },
            { offset: 1, color: colors[2] }
          ]),
          shadowBlur: 10,
          shadowOffsetX: 0,
          shadowColor: 'rgba(0, 0, 0, 0.2)'
        }
      },
      animationDelay: function(idx) {
        return idx * 10 + 200
      }
    },
    {
      name: '平均耗时',
      type: 'bar',
      barGap: '0%',
      barWidth: '10%',
      data: seriesAvg,
      itemStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
          { offset: 0, color: colors[3] },
          { offset: 1, color: colors[3] + '80' }
        ]),
        borderRadius: [0, 4, 4, 0]
      },
      emphasis: {
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
            { offset: 0, color: colors[3] },
            { offset: 1, color: colors[3] }
          ]),
          shadowBlur: 10,
          shadowOffsetX: 0,
          shadowColor: 'rgba(0, 0, 0, 0.2)'
        }
      },
      animationDelay: function(idx) {
        return idx * 10 + 300
      }
    },
    {
      name: '最小响应时间',
      type: 'bar',
      barGap: '0%',
      barWidth: '10%',
      data: seriesMin,
      itemStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
          { offset: 0, color: colors[4] },
          { offset: 1, color: colors[4] + '80' }
        ]),
        borderRadius: [0, 4, 4, 0]
      },
      emphasis: {
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
            { offset: 0, color: colors[4] },
            { offset: 1, color: colors[4] }
          ]),
          shadowBlur: 10,
          shadowOffsetX: 0,
          shadowColor: 'rgba(0, 0, 0, 0.2)'
        }
      },
      animationDelay: function(idx) {
        return idx * 10 + 400
      }
    },
    {
      name: '响应时间标准差',
      type: 'bar',
      barGap: '0%',
      barWidth: '10%',
      data: seriesStdDev,
      itemStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
          { offset: 0, color: colors[5] },
          { offset: 1, color: colors[5] + '80' }
        ]),
        borderRadius: [0, 4, 4, 0]
      },
      emphasis: {
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
            { offset: 0, color: colors[5] },
            { offset: 1, color: colors[5] }
          ]),
          shadowBlur: 10,
          shadowOffsetX: 0,
          shadowColor: 'rgba(0, 0, 0, 0.2)'
        }
      },
      animationDelay: function(idx) {
        return idx * 10 + 500
      }
    }
  ]

  // 如果需要显示访问量，添加访问量系列
  if (config.showCount) {
    series.push({
      name: '访问量',
      type: 'bar',
      barGap: '0%',
      barWidth: '10%',
      data: seriesCount,
      itemStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
          { offset: 0, color: colors[6] },
          { offset: 1, color: colors[6] + '80' }
        ]),
        borderRadius: [0, 4, 4, 0]
      },
      emphasis: {
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
            { offset: 0, color: colors[6] },
            { offset: 1, color: colors[6] }
          ]),
          shadowBlur: 10,
          shadowOffsetX: 0,
          shadowColor: 'rgba(0, 0, 0, 0.2)'
        }
      },
      animationDelay: function(idx) {
        return idx * 10 + 600
      }
    })
  }

  const option = {
    backgroundColor: 'transparent',
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      },
      backgroundColor: 'rgba(0, 0, 0, 0.7)',
      borderColor: 'transparent',
      textStyle: {
        color: '#fff',
        fontSize: 13
      },
      formatter: function(params) {
        let tooltip = `<div style="padding: 10px;">${params[0].name}<br/>`
        params.forEach(param => {
          const dotStyle = `style="display: inline-block; width: 8px; height: 8px; border-radius: 50%; background-color: ${param.color}; margin-right: 5px;"`
          // 确保值是数字后再调用 toFixed 方法
          const value = param.value
          // 对访问量做特殊处理，显示为整数
          if (param.seriesName === '访问量') {
            tooltip += `<div style="margin: 3px 0;"><span ${dotStyle}></span>${param.seriesName}: ${parseInt(value).toLocaleString()}</div>`
          } else {
            // 确保值是数字后再调用 toFixed 方法
            if (typeof value === 'number') {
              tooltip += `<div style="margin: 3px 0;"><span ${dotStyle}></span>${param.seriesName}: ${value.toFixed(2)}ms</div>`
            } else if (typeof value === 'string' && !isNaN(parseFloat(value))) {
              tooltip += `<div style="margin: 3px 0;"><span ${dotStyle}></span>${param.seriesName}: ${parseFloat(value).toFixed(2)}ms</div>`
            } else {
              tooltip += `<div style="margin: 3px 0;"><span ${dotStyle}></span>${param.seriesName}: ${value}</div>`
            }
          }
        })
        tooltip += '</div>'
        return tooltip
      }
    },
    legend: {
      data: ['最大响应时间', '90%分位响应时间', '中位数响应时间', '平均耗时', '最小响应时间', '响应时间标准差'],
      top: '4%',
      textStyle: {
        color: '#606266',
        fontSize: 12
      },
      itemWidth: 12,
      itemHeight: 12
    },
    grid: {
      left: '3%',
      right: '4%',
      top: '20%',
      bottom: '5%',
      containLabel: true
    },
    xAxis: {
      type: 'value',
      name: '响应时间(ms)',
      nameTextStyle: {
        color: '#606266',
        padding: [0, 0, 0, 40]
      },
      axisLabel: {
        color: '#606266'
      },
      splitLine: {
        lineStyle: {
          color: '#f0f0f0',
          type: 'dashed'
        }
      }
    },
    yAxis: {
      type: 'category',
      data: yAxisData,
      axisTick: {
        alignWithLabel: true
      },
      axisLabel: {
        fontSize: 12,
        color: '#606266'
      }
    },
    series: series,
    animationEasing: 'elasticOut',
    animationDelayUpdate: function(idx) {
      return idx * 5
    }
  }

  // 如果需要显示平均值参考线
  if (config.showAverage) {
    option.series.push({
      name: '整体平均耗时',
      type: 'line',
      symbol: 'circle',
      symbolSize: 8,
      data: Array(sortedData.length).fill(totalAvg),
      itemStyle: {
        color: '#722ed1'
      },
      lineStyle: {
        width: 2,
        type: 'dashed'
      },
      tooltip: {
        formatter: function() {
          return `整体平均耗时: ${parseFloat(totalAvg).toFixed(2)}ms`
        }
      }
    })

    // 更新图例
    option.legend.data.push('整体平均耗时')
  }

  chart.setOption(option)
  return chart
}
