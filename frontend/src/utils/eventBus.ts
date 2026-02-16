import mitt from 'mitt'

const emitter = mitt<{
  'refresh:query': void
  'refresh:anomaly': void
  'refresh:stats': void
  'refresh:sys': void
  'refresh:person': void
  // 地图联动事件
  'map:zoomTo': { longitude: number; latitude: number; zoom?: number }
  'map:filter': { districtId?: number; streetId?: number; categoryCode?: string; levelCode?: string }
  'map:showPerson': { personId: number }
  'map:showPersons': { personIds: number[] }
}>()

export { emitter }
export default emitter
