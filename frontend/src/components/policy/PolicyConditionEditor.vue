<template>
  <div class="condition-editor">
    <a-card title="政策条件" size="small">
      <template #extra>
        <a-space>
          <a-button size="small" @click="resetToOriginal">
            <undo-outlined /> 重置
          </a-button>
          <a-button type="primary" size="small" @click="saveConditions">
            <save-outlined /> 保存
          </a-button>
        </a-space>
      </template>
      
      <a-form layout="vertical">
        <!-- 户籍区县 -->
        <a-form-item label="户籍区县">
          <a-select
            v-model:value="editedConditions.districtIds"
            mode="multiple"
            placeholder="选择区县"
            style="width: 100%"
            :options="districtOptions"
          />
        </a-form-item>
        
        <!-- 残疾类别 -->
        <a-form-item label="残疾类别">
          <a-checkbox-group v-model:value="editedConditions.disabilityCategories">
            <a-checkbox value="LIMB">肢体</a-checkbox>
            <a-checkbox value="VISION">视力</a-checkbox>
            <a-checkbox value="HEARING">听力</a-checkbox>
            <a-checkbox value="SPEECH">言语</a-checkbox>
            <a-checkbox value="INTELLECTUAL">智力</a-checkbox>
            <a-checkbox value="MENTAL">精神</a-checkbox>
          </a-checkbox-group>
        </a-form-item>
        
        <!-- 残疾等级 -->
        <a-form-item label="残疾等级">
          <a-checkbox-group v-model:value="editedConditions.disabilityLevels">
            <a-checkbox value="1">一级</a-checkbox>
            <a-checkbox value="2">二级</a-checkbox>
            <a-checkbox value="3">三级</a-checkbox>
            <a-checkbox value="4">四级</a-checkbox>
          </a-checkbox-group>
        </a-form-item>
        
        <!-- 补贴要求 -->
        <a-form-item label="补贴要求">
          <a-space direction="vertical">
            <a-checkbox v-model:checked="editedConditions.hasCar">
              有残车补贴
            </a-checkbox>
            <a-checkbox v-model:checked="editedConditions.hasMedicalSubsidy">
              有医疗补贴
            </a-checkbox>
            <a-checkbox v-model:checked="editedConditions.hasPensionSubsidy">
              有养老补贴
            </a-checkbox>
            <a-checkbox v-model:checked="editedConditions.hasBlindCard">
              有盲人证
            </a-checkbox>
          </a-space>
        </a-form-item>
        
        <!-- 年龄范围 -->
        <a-form-item label="年龄范围">
          <a-slider
            v-model:value="ageRange"
            range
            :min="0"
            :max="100"
            style="width: 100%"
          />
          <div class="age-display">
            {{ editedConditions.ageMin || 0 }} 岁 - {{ editedConditions.ageMax || 100 }} 岁
          </div>
        </a-form-item>
      </a-form>
    </a-card>
    
    <a-card title="AI分析说明" size="small" style="margin-top: 16px;">
      <p style="color: #666; white-space: pre-wrap;">{{ conditions?.explanation || '暂无说明' }}</p>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { message } from 'ant-design-vue'
import { UndoOutlined, SaveOutlined } from '@ant-design/icons-vue'
import type { PolicyConditions } from '../../api/policy'

const emit = defineEmits<{
  (e: 'update', conditions: PolicyConditions): void
  (e: 'save', conditions: PolicyConditions): void
}>()

const props = defineProps<{
  conditions: PolicyConditions | null
}>()

const editedConditions = ref<PolicyConditions>({
  districtIds: [],
  disabilityCategories: [],
  disabilityLevels: [],
  hasCar: undefined,
  hasMedicalSubsidy: undefined,
  hasPensionSubsidy: undefined,
  hasBlindCard: undefined,
  ageMin: undefined,
  ageMax: undefined
})

const ageRange = computed({
  get: () => [
    editedConditions.value.ageMin || 0,
    editedConditions.value.ageMax || 100
  ],
  set: (val) => {
    editedConditions.value.ageMin = val[0]
    editedConditions.value.ageMax = val[1]
  }
})

const districtOptions = [
  { label: '浦东新区', value: 310115 },
  { label: '黄浦区', value: 310101 },
  { label: '静安区', value: 310106 },
  { label: '徐汇区', value: 310104 },
  { label: '长宁区', value: 310105 },
  { label: '普陀区', value: 310107 },
  { label: '虹口区', value: 310109 },
  { label: '杨浦区', value: 310110 },
  { label: '闵行区', value: 310112 },
  { label: '宝山区', value: 310113 },
  { label: '嘉定区', value: 310114 },
  { label: '金山区', value: 310116 },
  { label: '松江区', value: 310117 },
  { label: '青浦区', value: 310118 },
  { label: '奉贤区', value: 310120 },
  { label: '崇明区', value: 310151 }
]

const resetToOriginal = () => {
  if (props.conditions) {
    editedConditions.value = JSON.parse(JSON.stringify(props.conditions))
    message.info('已重置为AI提取的条件')
  }
}

const saveConditions = () => {
  emit('save', editedConditions.value)
  message.success('条件已保存')
}

watch(() => props.conditions, (newVal) => {
  if (newVal) {
    editedConditions.value = JSON.parse(JSON.stringify(newVal))
  }
}, { immediate: true, deep: true })

watch(editedConditions, (newVal) => {
  emit('update', newVal)
}, { deep: true })
</script>

<style scoped>
.condition-editor {
  padding: 16px;
}
.age-display {
  text-align: center;
  color: #1890ff;
  font-weight: 500;
  margin-top: 8px;
}
</style>
