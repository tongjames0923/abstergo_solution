export  function rangeQueryTransform(params) {
  const newParams = {...params};

  // 遍历所有参数
  for (const key in params) {
    // 检查参数名是否以Range结尾且值为数组
    if (key.endsWith('Range') && Array.isArray(params[key]) && params[key].length === 2) {
      // 提取基础字段名（去掉Range后缀）
      const baseKey = key.substring(0, key.length - 5);

      // 添加大于等于和小于等于的参数
      newParams[baseKey + 'GreaterThan'] = params[key][0];
      newParams[baseKey + 'LessThan'] = params[key][1];

      // 删除原来的Range参数
      delete newParams[key];
    }
  }

  return newParams;
}
