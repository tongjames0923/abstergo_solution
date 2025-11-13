import * as echarts from 'echarts'

/**
 * 初始化访问量图表
 * @param {HTMLElement} chartElement - 图表容器元素
 * @param {Array} chartData - 图表数据
 * @returns {echarts.ECharts} echarts实例
 */
export function initVisitCountChart(chartElement, chartData) {
  if (!chartElement) return null

  // 先销毁已有的图表实例
  const existingChart = echarts.getInstanceByDom(chartElement)
  if (existingChart) {
    existingChart.dispose()
  }

  const chart = echarts.init(chartElement)

  // 按操作次数排序，取前10个
  const sortedData = [...chartData].sort((a, b) =>
    parseFloat(b.operateCount) - parseFloat(a.operateCount))
    .slice(0, 10)

  // 为水平条形图交换数据顺序（因为ECharts的y轴是从下到上显示的）
  const yAxisData = sortedData.map(item => item.operateType).reverse()
  const seriesData = sortedData.map(item => parseFloat(item.operateCount)).reverse()

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
      formatter: function (params) {
        const param = params[0]
        return `${param.name}<br/>访问量: ${parseInt(param.value).toLocaleString()}`
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      top: '5%',
      bottom: '5%',
      containLabel: true
    },
    xAxis: {
      type: 'value',
      name: '访问量(次)',
      nameTextStyle: {
        color: '#606266',
        padding: [0, 0, 0, 40]
      },
      axisLabel: {
        color: '#606266',
        formatter: function (value) {
          if (value >= 1000) {
            return (value / 1000).toFixed(1) + 'k'
          }
          return value
        }
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
        name: '访问量',
        type: 'bar',
        barWidth: '60%',
        data: seriesData,
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
            {offset: 0, color: '#40a9ff'},
            {offset: 1, color: '#1890ff'}
          ]),
          borderRadius: [0, 6, 6, 0],
          shadowBlur: 8,
          shadowOffsetX: 3,
          shadowColor: 'rgba(24, 144, 255, 0.3)'
        },
        emphasis: {
          itemStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
              {offset: 0, color: '#69c0ff'},
              {offset: 1, color: '#40a9ff'}
            ]),
            shadowBlur: 15,
            shadowOffsetX: 5,
            shadowColor: 'rgba(24, 144, 255, 0.4)'
          }
        },
        label: {
          show: true,
          position: 'right',
          color: '#303133',
          fontWeight: 'bold',
          fontSize: 11,
          formatter: function (params) {
            // 在柱状图右侧显示数值，大于1000的显示为k单位
            const value = params.value
            if (value >= 1000) {
              return (value / 1000).toFixed(1) + 'k'
            }
            return value
          }
        },
        animationDelay: function (idx) {
          return idx * 30
        }
      }
    ],
    animationEasing: 'elasticOut',
    animationDelayUpdate: function (idx) {
      return idx * 5
    }
  }

  chart.setOption(option)
  return chart
}
