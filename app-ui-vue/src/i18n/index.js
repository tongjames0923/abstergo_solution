import Vue from 'vue'
import VueI18n from 'vue-i18n'
import Cookies from 'js-cookie'
import elementEnLocale from 'element-ui/lib/locale/lang/en'
import elementZhLocale from 'element-ui/lib/locale/lang/zh-CN'

import zh from './zh'
import en from './en'

Vue.use(VueI18n)

const messages = {
  zh: {
    ...zh,
    ...elementZhLocale
  },
  en: {
    ...en,
    ...elementEnLocale
  }
}

// 从cookie获取语言设置，默认中文
export function getLanguage() {
  return Cookies.get('language') || 'zh'
}

const i18n = new VueI18n({
  locale: getLanguage(),
  messages
})

export default i18n
