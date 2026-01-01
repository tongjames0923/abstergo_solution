import * as echarts from 'echarts'

/**
 * 初始化稳定性指数分布图表
 * @param {HTMLElement} chartElement - 图表容器元素
 * @param {Array} chartData - 图表数据
 * @returns {echarts.ECharts} echarts实例
 */
export function initStabilityChart(chartElement, chartData) {
  if (!chartElement) return null

  // 先销毁已有的图表实例
  const existingChart = echarts.getInstanceByDom(chartElement)
  if (existingChart) {
    existingChart.dispose()
  }

  const chart = echarts.init(chartElement)

  // 分段统计稳定性指数
  const segments = [
    { name: '优秀(≥85)', value: 0 },
    { name: '良好(60-84)', value: 0 },
    { name: '一般(45-59)', value: 0 },
    { name: '较差(<45)', value: 0 }
  ]

  chartData.forEach(item => {
    const index = item.stabilityIndex
    if (index >= 85) {
      segments[0].value++
    } else if (index >= 60) {
      segments[1].value++
    } else if (index >= 45) {
      segments[2].value++
    } else {
      segments[3].value++
    }
  })

  // 设置渐变颜色
  const colors = [
    new echarts.graphic.LinearGradient(0, 0, 0, 1, [
      { offset: 0, color: '#52c41a' },
      { offset: 1, color: '#73d13d' }
    ]),
    new echarts.graphic.LinearGradient(0, 0, 0, 1, [
      { offset: 0, color: '#1890ff' },
      { offset: 1, color: '#40a9ff' }
    ]),
    new echarts.graphic.LinearGradient(0, 0, 0, 1, [
      { offset: 0, color: '#faad14' },
      { offset: 1, color: '#ffc53d' }
    ]),
    new echarts.graphic.LinearGradient(0, 0, 0, 1, [
      { offset: 0, color: '#f5222d' },
      { offset: 1, color: '#ff4d4f' }
    ])
  ]

  const option = {
    backgroundColor: 'transparent',
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c} ({d}%)',
      backgroundColor: 'rgba(0, 0, 0, 0.7)',
      borderColor: 'transparent',
      textStyle: {
        color: '#fff',
        fontSize: 13
      }
    },
    legend: {
      bottom: '2%',
      left: 'center',
      textStyle: {
        color: '#606266',
        fontSize: 13
      }
    },
    series: [
      {
        name: '稳定性指数分布',
        type: 'pie',
        radius: ['40%', '70%'],
        center: ['50%', '45%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 12,
          borderColor: '#fff',
          borderWidth: 3,
          shadowBlur: 10,
          shadowOffsetX: 0,
          shadowColor: 'rgba(0, 0, 0, 0.2)'
        },
        label: {
          show: false
        },
        emphasis: {
          label: {
            show: true,
            fontSize: '16',
            fontWeight: 'bold',
            color: '#303133'
          },
          itemStyle: {
            shadowBlur: 20,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.3)'
          }
        },
        labelLine: {
          show: false
        },
        data: segments.map((item, index) => ({
          ...item,
          itemStyle: {
            color: colors[index]
          }
        })),
        animationType: 'scale',
        animationEasing: 'elasticOut',
        animationDelay: function(idx) {
          return Math.random() * 200
        }
      }
    ]
  }

  chart.setOption(option)
  return chart
}
