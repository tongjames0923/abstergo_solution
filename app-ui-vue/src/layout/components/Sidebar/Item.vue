<script>
export default {
  name: 'MenuItem',
  functional: true,
  props: {
    icon: {
      type: String,
      default: ''
    },
    title: {
      type: String,
      default: ''
    }
  },
  render(h, context) {
    const { icon, title } = context.props
    const vnodes = []

    if (icon) {
      if (icon.includes('el-icon')) {
        vnodes.push(<i class={[icon, 'sub-el-icon']} />)
      } else {
        vnodes.push(<svg-icon icon-class={icon}/>)
      }
    } else {
      // 无图标时保留等宽占位，保证所有菜单项文字水平对齐
      vnodes.push(<i class='menu-icon-placeholder' />)
    }

    if (title) {
      vnodes.push(<span slot='title'>{(title)}</span>)
    }
    return vnodes
  }
}
</script>

<style scoped>
.sub-el-icon {
  color: currentColor;
  width: 1em;
  height: 1em;
}

/* 无图标菜单项的占位：宽度与图标（1em）+ 间距（10px）一致，保证文字对齐 */
.menu-icon-placeholder {
  display: inline-block;
  width: 1em;
  height: 1em;
  margin-right: 10px;
  vertical-align: -2px;
}

.hideSidebar {
  .sub-el-icon,
  .svg-icon {
    width: 1rem;
    height: 1rem;
  }
}
</style>
