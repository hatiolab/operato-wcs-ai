# 建模器

## 建模器布局

说明建模器的布局。
![建模器-布局][layout]

1. 控制工具栏：聚集了控制还原、复制、排序、反转等组件的功能的工具栏。

1. 组件工具栏：聚集了可以被用在建模器的组件的工具栏。

1. 属性窗口：聚集了组件的属性的窗口。


[layout]: ./images/modeler-layout-01.png

## 控制工具栏

列出当前正在建模的场景的功能并简化建模所需的功能的工具栏。  
<span style="font-size: 13px; line-height:30px;">  （如果OS是Mac，请使用<kbd>Ctrl</kbd>键为<kbd class="dark">⌘</kbd>。）</span>
![控制工具栏][control-toolbar]

1. 撤销![撤销][撤销](<kbd>Ctrl</kbd> + <kbd>Z</kbd>) :  
  可以恢复用户最终编辑的内容的功能。

1. 重做![重做][重做](<kbd>Ctrl</kbd> + <kbd>Shift</kbd> + <kbd>Z</kbd>) :  
  重新执行通过撤销而取消的操作。

1. 剪切![剪切][剪切](<kbd>Ctrl</kbd> + <kbd>X</kbd>) :  
  将所选组件复制到剪贴板时，删除当前组件。

1. 复制![复制][复制](<kbd>Ctrl</kbd> + <kbd>C</kbd>) :  
  将所选组件复制到剪贴板。

1. 粘贴![粘贴][粘贴](<kbd>Ctrl</kbd> + <kbd>V</kbd>) :  
  将已复制的组件粘贴到剪贴板。

1. 删除![删除][删除](<kbd>Delete</kbd> or <kbd>Backspace</kbd>) :  
  删除所选组件。

1. 左对齐![左对齐][左对齐](<kbd>Alt</kbd> + <kbd>Shift</kbd> + <kbd>L</kbd>) :  
  以位于所选组件的最左边的组件为标准，向左对齐组件。

1. 居中对齐![居中对齐][居中对齐](<kbd>Alt</kbd> + <kbd>Shift</kbd> + <kbd>C</kbd>) :  
  以所选组件的中心坐标为标准，居中对齐组件的X坐标。

1. 右对齐![右对齐][右对齐](<kbd>Alt</kbd> + <kbd>Shift</kbd> + <kbd>R</kbd>) :  
  以位于所选组件的最右边的组件为标准，向右对齐组件。

1. 上对齐![上对齐][上对齐](<kbd>Alt</kbd> + <kbd>Shift</kbd> + <kbd>T</kbd>) :  
  以位于所选组件的最上方的组件为标准，向上对齐组件。

1. 居中对齐![居中对齐][居中对齐](<kbd>Alt</kbd> + <kbd>Shift</kbd> + <kbd>M</kbd>) :  
  以所选组件的中心坐标为标准，居中对齐组件的Y坐标。

1. 下对齐![下对齐][下对齐](<kbd>Alt</kbd> + <kbd>Shift</kbd> + <kbd>B</kbd>) :  
  以位于所选组件的最下方的组件为标准，向下对齐组件。

1. 水平均布![水平均布][水平均布](<kbd>Alt</kbd> + <kbd>Shift</kbd> + <kbd>H</kbd>) :  
  根据所选组件在水平方向两端的组件，将每个组件之间调整为相同的宽度。

1. 垂直均布![垂直均布][垂直均布](<kbd>Alt</kbd> + <kbd>Shift</kbd> + <kbd>V</kbd>) :  
  根据所选组件在垂直方向两端的组件，将每个组件之间调整为相同的高度。

1. 置为顶层![置为顶层][置为顶层](<kbd>Ctrl</kbd> + <kbd>Shift</kbd> + <kbd>F</kbd>) :  
  将所选组件的顺序推到最顶层。

1. 置为底层![置为底层][置为底层](<kbd>Ctrl</kbd> + <kbd>Shift</kbd> + <kbd>B</kbd>) :  
  将所选组件的顺序推到最底层。

1. 前移一层![前移一层][前移一层](<kbd>Ctrl</kbd> + <kbd>F</kbd>) :  
  将所选组件的顺序向前推动一层。

1. 后移一层![后移一层][后移一层](<kbd>Ctrl</kbd> + <kbd>B</kbd>) :  
  将所选组件的顺序向后推动一层。

1. 水平翻转![水平翻转][水平翻转](<kbd>Alt</kbd> + <kbd>Shift</kbd> + <kbd>X</kbd>) :  
  水平翻转所选组件。

1. 垂直翻转![垂直翻转][垂直翻转](<kbd>Alt</kbd> + <kbd>Shift</kbd> + <kbd>Y</kbd>) :  
  垂直翻转所选组件。

1. 顺时针旋转![顺时针旋转][顺时针旋转](<kbd>Alt</kbd> + <kbd>Shift</kbd> + <kbd>E</kbd>) :  
  将所选组件顺时针旋转90度。

1. 逆时针旋转![逆时针旋转][逆时针旋转](<kbd>Alt</kbd> + <kbd>Shift</kbd> + <kbd>W</kbd>) :  
  将所选组件逆时针旋转90度。

1. 打组![打组][打组](<kbd>Ctrl</kbd> + <kbd>G</kbd>) :  
  打组所选组件。

1. 解组![解组][解组](<kbd>Ctrl</kbd> + <kbd>Shift</kbd> + <kbd>G</kbd>) :  
  取消所选组。

1. 增加文本大小![增加文本大小][增加文本大小](<kbd>Shift</kbd> + <kbd>WheelUp</kbd>) :  
  增加所选组件的文本大小。

1. 减少文本大小![减少文本大小][减少文本大小](<kbd>Shift</kbd> + <kbd>WheelDown</kbd>) :  
  减少所选组件的文本大小。

1. 调整画布大小![调整画布大小][调整画布大小](<kbd>Ctrl</kbd> + <kbd>D</kbd>) :  
  将画布大小调整到画面。

1. 预览![预览][预览](<kbd>Ctrl</kbd> + <kbd>P</kbd>) :  
  在正在编辑的建模画面上，预览3D组件、动画和其他效果。    

1. 全屏![全屏][全屏](<kbd>F11</kbd>) :  
  隐藏所有工具栏和属性窗口，将建模画面转换为全屏。

1. 隐藏属性窗口![隐藏属性窗口][隐藏属性窗口](<kbd>Ctrl</kbd> + <kbd>H</kbd>) :  
  隐藏或显示属性窗口。



[control-toolbar]: ./images/control-toolbar.png

[撤销]: ./images/control-toolbar-02.png

[重做]: ./images/control-toolbar-03.png

[剪切]: ./images/control-toolbar-04.png

[复制]: ./images/control-toolbar-05.png

[粘贴]: ./images/control-toolbar-06.png

[删除]: ./images/control-toolbar-07.png

[左对齐]: ./images/control-toolbar-09.png

[居中对齐]: ./images/control-toolbar-10.png

[右对齐]: ./images/control-toolbar-11.png

[上对齐]: ./images/control-toolbar-12.png

[居中对齐]: ./images/control-toolbar-13.png

[下对齐]: ./images/control-toolbar-14.png

[水平均布]: ./images/control-toolbar-15.png

[垂直均布]: ./images/control-toolbar-16.png

[置为顶层]: ./images/control-toolbar-17.png

[置为底层]: ./images/control-toolbar-18.png

[前移一层]: ./images/control-toolbar-19.png

[后移一层]: ./images/control-toolbar-20.png

[水平翻转]: ./images/control-toolbar-21.png

[垂直翻转]: ./images/control-toolbar-22.png

[顺时针旋转]: ./images/control-toolbar-23.png

[逆时针旋转]: ./images/control-toolbar-24.png

[打组]: ./images/control-toolbar-25.png

[解组]: ./images/control-toolbar-26.png

[增加文本大小]: ./images/control-toolbar-27.png

[减少文本大小]: ./images/control-toolbar-28.png

[调整画布大小]: ./images/control-toolbar-29.png

[预览]: ./images/control-toolbar-32.png

[全屏]: ./images/control-toolbar-30.png

[隐藏属性窗口]: ./images/control-toolbar-31.png


## 组件工具栏

聚集了可以被用于建模器的组件的工具栏。如果在组件工具栏中，点击所需的组件，就会在建模器添加相关组件。  

- ![箭头][component-arrow] : 变更选择模式和移动模式。当按住<kbd>Space</kbd>时，将会动作为移动模式。

- ![线][component-line] : 聚集线组件。

- ![形状][component-shape] : 聚集形状组件。

- ![文本媒体][component-text-media] : 聚集文本媒体组件。

- ![图表仪表][component-chart-gauge] : 聚集图表仪表组件。

- ![表格][component-table] : 聚集表格组件。

- ![集装箱][component-container] : 聚集集装箱组件。

- ![数据源][component-datasource] : 聚集数据源组件。

- ![IOT][component-iot] : 聚集IOT组件。

- ![3D][component-3D] : 聚集3D组件。

- ![仓库][component-warehouse] : 聚集仓库组件。

- ![表单][component-form] : 聚集表单组件。

- ![其他][component-etc] : 聚集其他组件。

[component-toolbar]: ./images/component-toolbar.png
[component-arrow]: ./images/component-arrow.png
[component-line]: ./images/component-line.png
[component-shape]: ./images/component-shape.png
[component-text-media]: ./images/component-text-media.png
[component-chart-gauge]: ./images/component-chart-gauge.png
[component-table]: ./images/component-table.png
[component-container]: ./images/component-container.png
[component-datasource]: ./images/component-datasource.png
[component-iot]: ./images/component-iot.png
[component-3D]: ./images/component-3D.png
[component-warehouse]: ./images/component-warehouse.png
[component-form]: ./images/component-form.png
[component-etc]: ./images/component-etc.png

## 属性窗口
说明属性窗口的功能。  

1. ![形状属性页面][shape-property-tab]: 相关组件形状的属性。可以赋予ID、Class或指定宽度、高度。

1. ![样式属性页面][style-property-tab]: 相关组件样式的属性。可以指定颜色、字体等。

1. ![效果属性页面][effect-property-tab]: 相关组件的效果的属性。可以指定阴影、动画、点击时的事件。

1. ![特定属性页面][specific-property-tab]: 组件的固有属性。

1. ![数据属性页面][variable-property-tab]: 相关Scene的数据的属性。

[property-window]: ./images/property-window.png
[shape-property-tab]: ./images/shape-property.png
[style-property-tab]: ./images/style-property.png
[effect-property-tab]: ./images/effect-property.png
[specific-property-tab]: ./images/specific-property.png
[variable-property-tab]: ./images/variable-property.png
