<template>
  <div class="query-result">
    <div v-if="result" class="result-container">
      <!-- 结果概览 -->
      <a-card size="small" class="result-summary">
        <div class="summary-content">
          <div class="summary-item">
            <span class="label">政策：</span>
            <span class="value">{{ result.policyTitle }}</span>
          </div>
          <div class="summary-item">
            <span class="label">版本：</span>
            <a-tag color="blue">v{{ result.version }}</a-tag>
          </div>
          <div class="summary-item">
            <span class="label">符合条件人数：</span>
            <span class="value highlight">{{ result.total }} 人</span>
          </div>
        </div>
      </a-card>
      
      <!-- 人员列表 -->
      <a-table
        :columns="columns"
        :data-source="result.items"
        :pagination="{ pageSize: 10 }"
        size="small"
        row-key="personId"
        class="person-table"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'name'">
            <span class="masked">{{ record.nameMasked }}</span>
          </template>
          <template v-if="column.key === 'idNo'">
            <span class="masked">{{ record.idNoMasked }}</span>
          </template>
          <template v-if="column.key === 'disability'">
            {{ record.disabilityCategoryName }} / {{ record.disabilityLevelName }}
          </template>
          <template v-if="column.key === 'location'">
            {{ record.district }} / {{ record.street }}
          </template>
          <template v-if="column.key === 'benefits'">
            <a-space>
              <a-tag v-if="record.hasCar" size="small" color="green">残车</a-tag>
              <a-tag v-if="record.hasMedicalSubsidy" size="small" color="blue">医疗</a-tag>
              <a-tag v-if="record.hasPensionSubsidy" size="small" color="orange">养老</a-tag>
            </a-space>
          </template>
          <template v-if="column.key === 'action'">
            <a-button type="link" size="small" @click="viewDetail(record)">
              查看详情
            </a-button>
          </template>
        </template>
      </a-table>
    </div>
    
    <div v-else class="empty-result">
      <a-empty description="暂无查询结果" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import type { PolicyQueryResult } from '../../api/policy'

const props = defineProps<{
  result: PolicyQueryResult | null
}>()

const router = useRouter()

const columns = [
  { title: '姓名', key: 'name', width: 100 },
  { title: '身份证号', key: 'idNo', width: 150 },
  { title: '残疾类别/等级', key: 'disability', width: 150 },
  { title: '户籍', key: 'location', width: 150 },
  { title: '享受补贴', key: 'benefits', width: 200 },
  { title: '操作', key: 'action', width: 100 }
]

const viewDetail = (record: any) => {
  if (record.personId) {
    router.push(`/person/${record.personId}`)
  }
}
</script>

<style scoped>
.query-result {
  max-height: 70vh;
  overflow-y: auto;
}
.result-summary {
  margin-bottom: 16px;
  background: #f6ffed;
  border: 1px solid #b7eb8f;
}
.summary-content {
  display: flex;
  gap: 24px;
  align-items: center;
}
.summary-item {
  display: flex;
  align-items: center;
  gap: 8px;
}
.summary-item .label {
  color: #666;
}
.summary-item .value {
  font-weight: 500;
}
.summary-item .value.highlight {
  color: #52c41a;
  font-size: 16px;
}
.person-table {
  margin-top: 16px;
}
.masked {
  font-family: monospace;
}
.empty-result {
  padding: 48px;
  text-align: center;
}
</style>
