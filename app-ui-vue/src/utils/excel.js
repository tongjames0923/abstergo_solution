/**
 * 导出表格数据到Excel - 使用原始数据
 * @param {*} tableData - 表格原始数据
 * @param {*} columns - 表格列配置
 * @param {*} fileName - 文件名
 */
export function exportTableToExcel(tableData, columns, fileName) {
  // 动态导入xlsx库，避免增加打包体积
  import(/* webpackChunkName: "xlsx" */ 'xlsx').then(XLSX => {
    // 将表格数据转换为适合导出的格式
    const exportData = tableData.map(row => {
      const newRow = {}
      columns.forEach(column => {
        if (column.prop) {
          let value = row[column.prop]
          
            // 处理日期格式
          if (column.prop.includes('Time') || column.prop.includes('Date')) {
            value = value ? new Date(value).toLocaleString() : ''
          } 
          // 处理特殊值
          else if (value === null || value === undefined) {
            value = ''
          }
          // 处理状态列的特殊值转换
          else if (column.label === '状态' && typeof value === 'number') {
            switch(value) {
              case 1: value = '正常'; break
              case 0: value = '禁用'; break
              case 2: value = '锁定'; break
              default: value = '未知'
            }
          }
          // 对于其他值保持原样
          
          newRow[column.label] = value
        }
      })
      return newRow
    })
    
    // 创建工作表
    const ws = XLSX.utils.json_to_sheet(exportData)
    
    // 创建工作簿并添加工作表
    const wb = XLSX.utils.book_new()
    XLSX.utils.book_append_sheet(wb, ws, 'Sheet1')
    
    // 生成文件并下载
    XLSX.writeFile(wb, `${fileName}.xlsx`)
  })
}

/**
 * 导出JSON数据到Excel文件
 * @param {*} jsonData - 要导出的JSON数据
 * @param {*} keyMap - 字段映射关系，格式：{ '原始字段名': '显示标题' }
 * @param {*} fileName - 文件名
 */
export function exportJsonToExcel(jsonData, keyMap, fileName) {
  // 动态导入xlsx库，避免增加打包体积
  import(/* webpackChunkName: "xlsx" */ 'xlsx').then(XLSX => {
    // 将数据转换为工作表
    const ws = XLSX.utils.json_to_sheet(jsonData.map(item => {
      const newRow = {}
      Object.keys(keyMap).forEach(key => {
        newRow[keyMap[key]] = item[key]
      })
      return newRow
    }))
    
    // 创建工作簿并添加工作表
    const wb = XLSX.utils.book_new()
    XLSX.utils.book_append_sheet(wb, ws, 'Sheet1')
    
    // 生成文件并下载
    XLSX.writeFile(wb, `${fileName}.xlsx`)
  })
}

/**
 * 从Excel文件读取数据
 * @param {*} file - Excel文件对象
 * @param {*} callback - 读取完成后的回调函数
 */
export function readExcelFile(file, callback) {
  import(/* webpackChunkName: "xlsx" */ 'xlsx').then(XLSX => {
    const reader = new FileReader()
    reader.onload = function(e) {
      const data = new Uint8Array(e.target.result)
      const workbook = XLSX.read(data, { type: 'array' })
      
      // 默认读取第一个工作表
      const firstSheetName = workbook.SheetNames[0]
      const worksheet = workbook.Sheets[firstSheetName]
      const jsonData = XLSX.utils.sheet_to_json(worksheet)
      
      callback(jsonData)
    }
    reader.readAsArrayBuffer(file)
  })
}