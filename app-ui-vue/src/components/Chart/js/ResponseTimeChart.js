import * as echarts from 'echarts'

/**
 * 初始化响应时间分析图表
 * @param {HTMLElement} chartElement - 图表容器元素
 * @param {Array} chartData - 图表数据
 * @returns {echarts.ECharts} echarts实例
 */
export function initResponseTimeChart(chartElement, chartData) {
  if (!chartElement) return null

  // 先销毁已有的图表实例
  const existingChart = echarts.getInstanceByDom(chartElement)
  if (existingChart) {
    existingChart.dispose()
  }

  const chart = echarts.init(chartElement)

  // 按响应时间标准差排序，取前10个
  const sortedData = [...chartData].sort((a, b) =>
    parseFloat(b.operateTimeStdDev) - parseFloat(a.operateTimeStdDev))
    .slice(0, 10)

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

  // 使用统一的配色方案
  const colors = ['#ff7875', '#ffa940', '#fadb14', '#73d13d', '#40a9ff', '#b37feb']

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
        color: '#fff'
      },
      formatter: function(params) {
        let tooltip = `<div style="padding: 10px;">${params[0].name}<br/>`
        params.forEach(param => {
          const dotStyle = `style="display: inline-block; width: 8px; height: 8px; border-radius: 50%; background-color: ${param.color}; margin-right: 5px;"`
          tooltip += `<div style="margin: 3px 0;"><span ${dotStyle}></span>${param.seriesName}: ${param.value.toFixed(2)}ms</div>`
        })
        tooltip += '</div>'
        return tooltip
      }
    },
    legend: {
      data: ['最大响应时间', '90%分位响应时间', '中位数响应时间', '平均耗时', '最小响应时间', '响应时间标准差'],
      top: '2%',
      textStyle: {
        color: '#606266',
        fontSize: 12
      },
      itemWidth: 10,
      itemHeight: 10
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
    series: [
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
    ],
    animationEasing: 'elasticOut',
    animationDelayUpdate: function(idx) {
      return idx * 5
    }
  }

  chart.setOption(option)
  return chart
}