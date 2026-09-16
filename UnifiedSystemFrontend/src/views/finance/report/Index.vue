<template>
  <div class="page-container">
    <div class="page-toolbar">
      <h2 class="page-title">财务报表</h2>
    </div>
    <a-card class="content-card">
      <a-row :gutter="24">
        <a-col :span="8" v-for="report in reports" :key="report.key">
          <a-card hoverable class="report-card" @click="openReport(report)">
            <div class="report-card-content">
              <component :is="report.icon" class="report-icon" :style="{ color: report.color }" />
              <h3>{{ report.title }}</h3>
              <p>{{ report.desc }}</p>
            </div>
          </a-card>
        </a-col>
      </a-row>
    </a-card>

    <!-- 报表弹窗 -->
    <a-modal v-model:open="reportModalVisible" :title="currentReport?.title" @ok="generateReport" :confirm-loading="generating" :maskClosable="false">
      <a-form :label-col="{ span: 6 }">
        <a-form-item label="报表类型">
          <a-input :value="currentReport?.title" disabled />
        </a-form-item>
        <a-form-item label="开始日期">
          <a-date-picker v-model:value="reportForm.startDate" style="width:100%" />
        </a-form-item>
        <a-form-item label="结束日期">
          <a-date-picker v-model:value="reportForm.endDate" style="width:100%" />
        </a-form-item>
        <a-form-item label="备注">
          <a-textarea v-model:value="reportForm.remark" :rows="2" />
        </a-form-item>
      </a-form>
      <a-alert
        message="功能开发中 - 由 JimuReport 引擎提供"
        type="info"
        show-icon
        style="margin-top: 8px"
      />
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, shallowRef } from 'vue'
import {
  BarChartOutlined,
  PieChartOutlined,
  LineChartOutlined
} from '@ant-design/icons-vue'
import { message } from 'ant-design-vue'
import request from '@/api/request'
import dayjs from 'dayjs'

const reports = [
  { key: 'profit', title: '利润表', desc: '反映企业在一定会计期间经营成果的报表', color: '#1677ff', icon: shallowRef(BarChartOutlined) },
  { key: 'balance', title: '资产负债表', desc: '反映企业在某一特定日期全部资产、负债和所有者权益情况的会计报表', color: '#52c41a', icon: shallowRef(PieChartOutlined) },
  { key: 'cashflow', title: '现金流量表', desc: '反映企业现金流入和流出情况的报表', color: '#fa8c16', icon: shallowRef(LineChartOutlined) }
]

const reportModalVisible = ref(false)
const generating = ref(false)
const currentReport = ref<any>(null)
const reportForm = reactive({ startDate: null, endDate: null, remark: '' })

function openReport(report: any) {
  currentReport.value = report
  reportForm.startDate = null; reportForm.endDate = null; reportForm.remark = ''
  reportModalVisible.value = true
}

async function generateReport() {
  if (!reportForm.startDate || !reportForm.endDate) {
    message.warning('请选择日期范围')
    return
  }
  generating.value = true
  try {
    await request.post('/finance/reports/generate', {
      type: currentReport.value?.key,
      startDate: dayjs(reportForm.startDate).format('YYYY-MM-DD'),
      endDate: dayjs(reportForm.endDate).format('YYYY-MM-DD'),
      remark: reportForm.remark
    })
    message.success('报表生成请求已提交')
    reportModalVisible.value = false
  } catch { } finally { generating.value = false }
}
</script>

<style scoped lang="less">
@import '@/assets/styles/page.less';

.report-card {
  text-align: center;
  cursor: pointer;
  transition: box-shadow 0.3s;

  &:hover {
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.12);
  }
}

.report-card-content {
  padding: 20px 0;

  h3 {
    margin: 12px 0 8px;
    font-size: 18px;
    color: #1f2937;
  }

  p {
    margin: 0;
    color: #999;
    font-size: 13px;
  }
}

.report-icon {
  font-size: 48px;
}
</style>
