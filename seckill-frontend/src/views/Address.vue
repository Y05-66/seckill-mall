<template>
  <div class="address-page container">
    <div class="page-header">
      <h2>收货地址</h2>
      <button class="add-btn" @click="openAddDialog">+ 新增地址</button>
    </div>

    <div v-if="addressList.length > 0" class="address-list">
      <div v-for="addr in addressList" :key="addr.id" class="address-card">
        <div class="addr-info">
          <div class="addr-top">
            <span class="addr-name">{{ addr.receiverName }}</span>
            <span class="addr-phone">{{ addr.receiverPhone }}</span>
            <span v-if="addr.isDefault === 1" class="addr-default">默认</span>
          </div>
          <div class="addr-detail">
            {{ addr.province }}{{ addr.city }}{{ addr.district }}{{ addr.detailAddress }}
          </div>
        </div>
        <div class="addr-actions">
          <button v-if="addr.isDefault !== 1" @click="handleSetDefault(addr)">设为默认</button>
          <button @click="openEditDialog(addr)">编辑</button>
          <button @click="handleDelete(addr)">删除</button>
        </div>
      </div>
    </div>

    <div v-else class="empty">
      <p>暂无收货地址</p>
      <button class="add-btn" @click="openAddDialog">+ 添加地址</button>
    </div>

    <!-- 地址编辑弹窗 -->
    <div class="modal-mask" v-if="showDialog" @click.self="showDialog = false">
      <div class="modal">
        <div class="modal-title">{{ isEdit ? '编辑地址' : '新增地址' }}</div>
        <div class="form-group">
          <label>收货人</label>
          <input v-model="form.receiverName" placeholder="请输入收货人姓名" />
        </div>
        <div class="form-group">
          <label>手机号</label>
          <input v-model="form.receiverPhone" placeholder="请输入手机号" />
        </div>
        <div class="form-group">
          <label>所在地区</label>
          <div class="region-selects">
            <select v-model="form.province" @change="onProvinceChange" class="region-select">
              <option value="">请选择省份</option>
              <option v-for="p in provinces" :key="p" :value="p">{{ p }}</option>
            </select>
            <select v-model="form.city" @change="onCityChange" class="region-select">
              <option value="">请选择城市</option>
              <option v-for="c in cities" :key="c" :value="c">{{ c }}</option>
            </select>
            <select v-model="form.district" class="region-select">
              <option value="">请选择区县</option>
              <option v-for="d in districts" :key="d" :value="d">{{ d }}</option>
            </select>
          </div>
        </div>
        <div class="form-group">
          <label>详细地址</label>
          <input v-model="form.detailAddress" placeholder="请输入详细地址" />
        </div>
        <div class="modal-actions">
          <button class="btn-cancel" @click="showDialog = false">取消</button>
          <button class="btn-confirm" @click="handleSave">保存</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAddressList, addAddress, updateAddress, deleteAddress, setDefaultAddress } from '../api/address'

const addressList = ref([])
const showDialog = ref(false)
const isEdit = ref(false)
const editId = ref(null)
const form = ref({
  receiverName: '',
  receiverPhone: '',
  province: '',
  city: '',
  district: '',
  detailAddress: ''
})

// 省市区数据（常用地区）
const regionData = {
  '北京市': { '北京市': ['东城区','西城区','朝阳区','丰台区','石景山区','海淀区','门头沟区','房山区','通州区','顺义区','昌平区','大兴区'] },
  '上海市': { '上海市': ['黄浦区','徐汇区','长宁区','静安区','普陀区','虹口区','杨浦区','闵行区','宝山区','嘉定区','浦东新区','松江区'] },
  '广东省': {
    '广州市': ['越秀区','海珠区','荔湾区','天河区','白云区','黄埔区','番禺区','花都区','南沙区'],
    '深圳市': ['罗湖区','福田区','南山区','宝安区','龙岗区','盐田区','龙华区','坪山区','光明区'],
    '东莞市': ['莞城街道','南城街道','东城街道','万江街道'],
    '佛山市': ['禅城区','南海区','顺德区','三水区','高明区'],
    '珠海市': ['香洲区','金湾区','斗门区'],
    '中山市': ['石岐街道','东区街道','西区街道','南区街道'],
  },
  '浙江省': {
    '杭州市': ['上城区','拱墅区','西湖区','滨江区','萧山区','余杭区','临平区','富阳区'],
    '宁波市': ['海曙区','江北区','北仑区','鄞州区','奉化区'],
    '温州市': ['鹿城区','龙湾区','瓯海区','洞头区'],
    '嘉兴市': ['南湖区','秀洲区'],
    '绍兴市': ['越城区','柯桥区','上虞区'],
  },
  '江苏省': {
    '南京市': ['玄武区','秦淮区','建邺区','鼓楼区','浦口区','栖霞区','雨花台区','江宁区'],
    '苏州市': ['虎丘区','吴中区','相城区','姑苏区','吴江区'],
    '无锡市': ['锡山区','惠山区','滨湖区','梁溪区','新吴区'],
    '常州市': ['天宁区','钟楼区','新北区','武进区'],
    '南通市': ['崇川区','通州区','海门区'],
  },
  '四川省': {
    '成都市': ['锦江区','青羊区','金牛区','武侯区','成华区','龙泉驿区','青白江区','新都区','温江区','双流区'],
    '绵阳市': ['涪城区','游仙区','安州区'],
    '德阳市': ['旌阳区','罗江区'],
  },
  '湖北省': {
    '武汉市': ['江岸区','江汉区','硚口区','汉阳区','武昌区','青山区','洪山区','东西湖区','蔡甸区','江夏区'],
    '宜昌市': ['西陵区','伍家岗区','点军区','猇亭区'],
    '襄阳市': ['襄城区','樊城区','襄州区'],
  },
  '湖南省': {
    '长沙市': ['芙蓉区','天心区','岳麓区','开福区','雨花区','望城区'],
    '株洲市': ['荷塘区','芦淞区','石峰区','天元区'],
    '湘潭市': ['雨湖区','岳塘区'],
  },
  '福建省': {
    '福州市': ['鼓楼区','台江区','仓山区','马尾区','晋安区'],
    '厦门市': ['思明区','海沧区','湖里区','集美区','同安区','翔安区'],
    '泉州市': ['鲤城区','丰泽区','洛江区','泉港区'],
  },
  '山东省': {
    '济南市': ['历下区','市中区','槐荫区','天桥区','历城区','长清区'],
    '青岛市': ['市南区','市北区','黄岛区','崂山区','李沧区','城阳区'],
    '烟台市': ['芝罘区','福山区','牟平区','莱山区'],
  },
  '河南省': {
    '郑州市': ['中原区','二七区','管城回族区','金水区','上街区','惠济区'],
    '洛阳市': ['老城区','西工区','瀍河回族区','涧西区','洛龙区'],
  },
  '辽宁省': {
    '沈阳市': ['和平区','沈河区','大东区','皇姑区','铁西区','苏家屯区','浑南区'],
    '大连市': ['中山区','西岗区','沙河口区','甘井子区','旅顺口区','金州区'],
  },
  '天津市': { '天津市': ['和平区','河东区','河西区','南开区','河北区','红桥区','东丽区','西青区','津南区','北辰区','武清区','宝坻区','滨海新区'] },
  '重庆市': { '重庆市': ['万州区','涪陵区','渝中区','大渡口区','江北区','沙坪坝区','九龙坡区','南岸区','北碚区','渝北区','巴南区','长寿区'] },
  '河北省': {
    '石家庄市': ['长安区','桥西区','新华区','裕华区','井陉矿区','藁城区','鹿泉区','栾城区'],
    '唐山市': ['路南区','路北区','古冶区','开平区','丰南区','丰润区'],
  },
  '安徽省': {
    '合肥市': ['瑶海区','庐阳区','蜀山区','包河区'],
    '芜湖市': ['镜湖区','弋江区','鸠江区','湾沚区','繁昌区'],
  },
  '江西省': {
    '南昌市': ['东湖区','西湖区','青云谱区','青山湖区','新建区','红谷滩区'],
  },
  '广西壮族自治区': {
    '南宁市': ['兴宁区','青秀区','江南区','西乡塘区','良庆区','邕宁区'],
    '柳州市': ['城中区','鱼峰区','柳南区','柳北区'],
  },
  '云南省': {
    '昆明市': ['五华区','盘龙区','官渡区','西山区','东川区','呈贡区'],
  },
  '贵州省': {
    '贵阳市': ['南明区','云岩区','花溪区','乌当区','白云区','观山湖区'],
  },
  '陕西省': {
    '西安市': ['新城区','碑林区','莲湖区','灞桥区','未央区','雁塔区','阎良区','临潼区','长安区'],
  },
  '山西省': {
    '太原市': ['小店区','迎泽区','杏花岭区','尖草坪区','万柏林区','晋源区'],
  },
  '黑龙江省': {
    '哈尔滨市': ['道里区','南岗区','道外区','平房区','松北区','香坊区'],
  },
  '吉林省': {
    '长春市': ['南关区','宽城区','朝阳区','二道区','绿园区','双阳区'],
  },
  '甘肃省': {
    '兰州市': ['城关区','七里河区','西固区','安宁区','红古区'],
  },
  '海南省': {
    '海口市': ['秀英区','龙华区','琼山区','美兰区'],
    '三亚市': ['海棠区','吉阳区','天涯区','崖州区'],
  },
}

/** 省份列表 */
const provinces = computed(() => Object.keys(regionData))
/** 根据选中省份获取城市列表 */
const cities = computed(() => {
  if (!form.value.province) return []
  return Object.keys(regionData[form.value.province] || {})
})
/** 根据选中城市获取区县列表 */
const districts = computed(() => {
  if (!form.value.province || !form.value.city) return []
  return regionData[form.value.province]?.[form.value.city] || []
})

/** 省份变更时清空城市和区县 */
const onProvinceChange = () => {
  form.value.city = ''
  form.value.district = ''
}
/** 城市变更时清空区县 */
const onCityChange = () => {
  form.value.district = ''
}

onMounted(loadAddresses)

async function loadAddresses() {
  try {
    const res = await getAddressList()
    addressList.value = res.data || []
  } catch (e) {}
}

const openAddDialog = () => {
  isEdit.value = false
  editId.value = null
  form.value = { receiverName: '', receiverPhone: '', province: '', city: '', district: '', detailAddress: '' }
  showDialog.value = true
}

const openEditDialog = (addr) => {
  isEdit.value = true
  editId.value = addr.id
  form.value = {
    receiverName: addr.receiverName,
    receiverPhone: addr.receiverPhone,
    province: addr.province,
    city: addr.city,
    district: addr.district,
    detailAddress: addr.detailAddress
  }
  showDialog.value = true
}

const handleSave = async () => {
  // 表单验证
  if (!form.value.receiverName?.trim()) {
    ElMessage.warning('请填写收货人姓名')
    return
  }
  if (form.value.receiverName.trim().length > 20) {
    ElMessage.warning('收货人姓名不能超过20个字符')
    return
  }
  if (!form.value.receiverPhone?.trim()) {
    ElMessage.warning('请填写手机号')
    return
  }
  if (!/^1[3-9]\d{9}$/.test(form.value.receiverPhone.trim())) {
    ElMessage.warning('请输入正确的手机号')
    return
  }
  if (!form.value.province?.trim()) {
    ElMessage.warning('请填写省份')
    return
  }
  if (!form.value.city?.trim()) {
    ElMessage.warning('请填写城市')
    return
  }
  if (!form.value.detailAddress?.trim()) {
    ElMessage.warning('请填写详细地址')
    return
  }
  if (form.value.detailAddress.trim().length < 5) {
    ElMessage.warning('详细地址至少5个字符')
    return
  }

  try {
    if (isEdit.value) {
      await updateAddress(editId.value, form.value)
      ElMessage.success('修改成功')
    } else {
      await addAddress(form.value)
      ElMessage.success('添加成功')
    }
    showDialog.value = false
    await loadAddresses()
  } catch (e) {}
}

const handleDelete = async (addr) => {
  try {
    await ElMessageBox.confirm('确认删除该地址？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
  } catch { return }
  try {
    await deleteAddress(addr.id)
    await loadAddresses()
    ElMessage.success('地址已删除')
  } catch (e) {}
}

const handleSetDefault = async (addr) => {
  try {
    await setDefaultAddress(addr.id)
    await loadAddresses()
  } catch (e) {}
}
</script>

<style scoped>
.address-page {}

.page-header {
  display: flex; justify-content: space-between; align-items: center;
  margin-bottom: 20px;
}
.page-header h2 { font-size: 22px; font-weight: 700; }
.add-btn {
  padding: 8px 20px; background: var(--red); color: white;
  border: none; border-radius: 6px; font-size: 14px;
  font-weight: 600; cursor: pointer;
}
.add-btn:hover { background: var(--red-dark); }

.address-list { display: flex; flex-direction: column; gap: 12px; }
.address-card {
  display: flex; justify-content: space-between; align-items: center;
  background: var(--white); padding: 16px 20px;
  border-radius: var(--radius); box-shadow: var(--shadow);
}
.addr-info { flex: 1; }
.addr-top { display: flex; align-items: center; gap: 12px; margin-bottom: 8px; }
.addr-name { font-size: 16px; font-weight: 600; }
.addr-phone { font-size: 14px; color: var(--text-sub); }
.addr-default {
  padding: 2px 8px; background: var(--red-light); color: var(--red);
  border-radius: 4px; font-size: 12px; font-weight: 600;
}
.addr-detail { font-size: 14px; color: var(--text-sub); }

.addr-actions { display: flex; gap: 8px; }
.addr-actions button {
  padding: 6px 12px; border: 1px solid var(--border);
  border-radius: 4px; background: white; cursor: pointer;
  font-size: 12px;
}
.addr-actions button:hover { color: var(--red); border-color: var(--red); }

.empty {
  text-align: center; padding: 60px 0;
  background: var(--white); border-radius: var(--radius);
  box-shadow: var(--shadow);
}
.empty p { font-size: 16px; color: var(--text-sub); margin-bottom: 20px; }

.modal-mask {
  position: fixed; top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0,0,0,0.5); display: flex;
  align-items: center; justify-content: center; z-index: 1000;
}
.modal {
  background: white; border-radius: 12px; padding: 24px;
  width: 450px;
}
.modal-title { font-size: 18px; font-weight: 700; margin-bottom: 20px; }
.form-group { margin-bottom: 14px; }
.form-group label { display: block; font-size: 13px; color: var(--text-sub); margin-bottom: 6px; }
.form-group input {
  width: 100%; padding: 10px 12px; border: 1px solid var(--border);
  border-radius: 6px; font-size: 14px; outline: none;
}
.form-group input:focus { border-color: var(--red); }
.region-selects {
  display: flex; gap: 8px;
}
.region-select {
  flex: 1; padding: 10px 8px; border: 1px solid var(--border);
  border-radius: 6px; font-size: 13px; outline: none;
  background: white; cursor: pointer;
}
.region-select:focus { border-color: var(--red); }
.modal-actions { display: flex; gap: 10px; margin-top: 20px; }
.btn-cancel {
  flex: 1; padding: 10px; border: 1px solid var(--border);
  border-radius: 6px; background: white; cursor: pointer; font-size: 14px;
}
.btn-confirm {
  flex: 1; padding: 10px; border: none;
  border-radius: 6px; background: var(--red); color: white;
  cursor: pointer; font-size: 14px; font-weight: 600;
}
</style>
