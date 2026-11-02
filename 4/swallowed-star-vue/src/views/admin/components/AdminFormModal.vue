<template>
  <div v-if="open" class="adm-mask" @click.self="close">
    <div class="adm-modal">
      <div class="adm-modal-head">
        <h3>{{ title }}</h3>
        <span class="grow"></span>
        <button class="x" type="button" @click="close">×</button>
      </div>

      <div class="adm-modal-body">
        <div class="adm-form">
          <label
            v-for="f in fields"
            :key="f.key"
            class="adm-field"
            :class="{ wide: f.span === 2 || f.type === 'textarea', check: f.type === 'checkbox' }"
          >
            <template v-if="f.type === 'checkbox'">
              <input v-model="local[f.key]" type="checkbox">
              <span>{{ f.label }}</span>
            </template>

            <template v-else>
              <span>{{ f.label }}<span v-if="f.required" class="req"> *</span></span>

              <select v-if="f.type === 'select'" v-model="local[f.key]" class="adm-ipt sel" style="width:100%">
                <option value="">{{ f.placeholder || '请选择' }}</option>
                <option v-for="o in f.options || []" :key="o.value" :value="o.value">{{ o.label }}</option>
              </select>

              <textarea
                v-else-if="f.type === 'textarea'"
                v-model="local[f.key]"
                class="adm-ipt"
                :rows="f.rows || 3"
                :maxlength="f.maxlength"
                :placeholder="f.placeholder || ''"
              ></textarea>

              <input
                v-else-if="f.type === 'number'"
                v-model.number="local[f.key]"
                class="adm-ipt"
                type="number"
                :min="f.min"
                :max="f.max"
                :step="f.step || 1"
                :placeholder="f.placeholder || ''"
              >

              <input
                v-else
                v-model="local[f.key]"
                class="adm-ipt"
                :type="f.type === 'password' ? 'password' : 'text'"
                :maxlength="f.maxlength"
                :placeholder="f.placeholder || ''"
              >

              <span v-if="f.hint" class="adm-hint">{{ f.hint }}</span>
            </template>
          </label>
        </div>

        <p v-if="error" class="adm-field err" style="margin-top:12px">{{ error }}</p>
      </div>

      <div class="adm-modal-foot">
        <button class="adm-btn" type="button" @click="close">取 消</button>
        <button class="adm-btn gold" type="button" :disabled="saving" @click="submit">
          {{ saving ? '保存中…' : '保 存' }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, watch } from 'vue'

const props = defineProps({
  open: { type: Boolean, default: false },
  title: { type: String, default: '编辑' },
  /** 字段定义：{ key, label, type, options, required, min, max, step, maxlength, placeholder, hint, span } */
  fields: { type: Array, default: () => [] },
  /** 初始值（打开弹窗时按此对象克隆） */
  initial: { type: Object, default: () => ({}) },
  saving: { type: Boolean, default: false }
})

const emit = defineEmits(['submit', 'cancel'])

const local = reactive({})
const error = ref('')

watch(
  () => [props.open, props.initial],
  () => {
    if (!props.open) return
    Object.keys(local).forEach(k => delete local[k])
    props.fields.forEach(f => {
      const v = props.initial ? props.initial[f.key] : undefined
      local[f.key] = v === undefined || v === null ? (f.type === 'checkbox' ? false : '') : v
    })
    error.value = ''
  },
  { immediate: true }
)

function close() {
  error.value = ''
  emit('cancel')
}

function submit() {
  for (const f of props.fields) {
    if (!f.required) continue
    const v = local[f.key]
    if (v === '' || v === null || v === undefined) {
      error.value = `请填写${f.label}`
      return
    }
  }
  error.value = ''
  emit('submit', { ...local })
}
</script>
