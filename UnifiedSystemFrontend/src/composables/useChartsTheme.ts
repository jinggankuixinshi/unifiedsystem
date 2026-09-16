import * as echarts from 'echarts/core'
import { BarChart, LineChart, PieChart } from 'echarts/charts'
import { GridComponent, TooltipComponent, LegendComponent, TitleComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'

echarts.use([BarChart, LineChart, PieChart, GridComponent, TooltipComponent, LegendComponent, TitleComponent, CanvasRenderer])

export const CHART_COLORS = ['#1e3a5f', '#5b8c5a', '#d4953a', '#b44a4a', '#4b7e9e', '#8fbc8f']

export function useChartsTheme() {
  return {
    colors: CHART_COLORS,
    tooltip: {
      backgroundColor: '#ffffff',
      borderColor: '#e5e7eb',
      borderWidth: 1,
      boxShadow: '0 4px 12px rgba(0,0,0,0.08)',
      textStyle: { color: '#4b5563', fontSize: 12 }
    },
    grid: {
      left: '3%', right: '4%', bottom: '3%', top: '12%', containLabel: true
    },
    legend: {
      textStyle: { color: '#4b5563', fontSize: 12 },
      icon: 'circle',
      itemWidth: 8,
      itemHeight: 8
    },
    xAxis: {
      axisLine: { lineStyle: { color: '#e5e7eb' } },
      axisTick: { show: false },
      axisLabel: { color: '#9ca3af', fontSize: 11 },
      splitLine: { show: false }
    },
    yAxis: {
      axisLine: { show: false },
      axisTick: { show: false },
      axisLabel: { color: '#9ca3af', fontSize: 11 },
      splitLine: {
        lineStyle: { color: '#f3f4f6', type: 'dashed' as const }
      }
    }
  }
}
