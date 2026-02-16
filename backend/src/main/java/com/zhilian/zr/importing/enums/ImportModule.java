package com.zhilian.zr.importing.enums;

import java.util.Arrays;
import java.util.List;

public enum ImportModule {
    
    DISABLED_CERT("disabled_cert", "残疾人认证", "T_IMP_DISABLED_CERT", "基本保障", 
        Arrays.asList("姓名", "证件号码", "年龄", "性别", "区县", "街道", "居委", "户籍类别", "状态", 
            "残疾类别", "残疾等级", "双侧或单侧", "户口地址", "联系地址", "联系电话", "工作/就业", 
            "初次申领日期", "业务申请类型", "申请日期", "备注")),
    
    DISABLED_MGMT("disabled_mgmt", "残疾管理", "T_IMP_DISABLED_MGMT", "基本保障",
        Arrays.asList("姓名", "证件号", "户籍区县", "户籍区", "居住地", "联系电话", "联系地址", 
            "车牌号", "车辆类型", "初次发放日期", "上牌日期", "重置日期", "残疾类型号", "状态", 
            "投保情况", "年检情况", "初次认定日期", "补贴月份", "补贴金额")),
    
    REHAB_SUBSIDY("rehab_subsidy", "康复医疗/器材补助", "T_IMP_REHAB_SUBSIDY", "医疗康复",
        Arrays.asList("姓名", "身份证号", "户籍区县", "户籍街道", "残疾类别", "残疾等级", 
            "医保办法", "账户信息", "回盘日期")),
    
    PENSION_SUBSIDY("pension_subsidy", "养老补助", "T_IMP_PENSION_SUBSIDY", "基本保障",
        Arrays.asList("姓名", "账户区县", "账户街镇", "身份证号", "性别", "缴费起始年月", 
            "缴费截止年月", "补助金额")),
    
    PERSONAL_CERT("personal_cert", "个人证管理", "T_IMP_PERSONAL_CERT", "证件管理",
        Arrays.asList("个人标识", "姓名", "证件号码", "申领日期", "街镇", "发证区", 
            "审核日期", "是否制证", "代办人证", "快递单号", "审批状态")),
    
    FAMILY_SITUATION("family_situation", "残疾家庭情况", "T_IMP_FAMILY_SITUATION", "家庭服务",
        Arrays.asList("居委会", "姓名", "残疾证号", "残疾类别", "等级", "改造地点", "改造类型", 
            "改造进度", "改造单位", "改造金额", "改造负责人", "联系方式")),
    
    LOAN_CARD("loan_card", "借信卡", "T_IMP_LOAN_CARD", "其他服务",
        Arrays.asList("姓名", "生日", "残疾人证号", "户籍地址", "家庭住址", "电话", 
            "申请的套餐种类（固定/流量/宽带）")),
    
    CHRONIC_DISEASE("chronic_disease", "慢性病/康护", "T_IMP_CHRONIC_DISEASE", "医疗康复",
        Arrays.asList("姓名", "证件号", "户籍区县", "户籍街道", "残疾类别", "年龄", 
            "服务人员姓名", "服务人员出生年月", "服务人员联系方式", "服务人员服务机构")),
    
    INSTITUTION_CARE("institution_care", "机构养护", "T_IMP_INSTITUTION_CARE", "医疗康复",
        Arrays.asList("姓名", "证件号", "户籍区县", "户籍街道", "残疾类别", "年龄", 
            "护理机构", "人住审核日期", "退出审核日期", "退出原因", "补贴月份", "补贴金额")),
    
    COCHLEAR_IMPLANT("cochlear_implant", "人工耳蜗", "T_IMP_COCHLEAR_IMPLANT", "医疗康复",
        Arrays.asList("街镇", "姓名", "证件号", "残疾类别", "联系方式", "年龄", "申请日期", 
            "手术日期", "开机日期", "手术医院", "植入耳", "启用耳", "手术金额", "自费金额", "补贴金额")),
    
    REHAB_ORTHODONTICS("rehab_orthodontics", "康复体矫", "T_IMP_REHAB_ORTHODONTICS", "医疗康复",
        Arrays.asList("街镇", "姓名", "证件号", "残疾类别", "联系方式", "年龄", "基因测验结果", 
            "申请时间", "检测医院", "检测日期", "补贴金额")),
    
    CHILD_REHAB("child_rehab", "儿童康复矫正", "T_IMP_CHILD_REHAB", "医疗康复",
        Arrays.asList("姓名", "证件号", "户籍区县", "户籍街道", "年龄", "残疾类别", "宝宝卡类别", 
            "出生日期", "申请日期", "评估医院", "康复机构", "机构地址", "补贴金额", "补贴年龄")),
    
    TRAFFIC_SUBSIDY("traffic_subsidy", "交通补贴", "T_IMP_TRAFFIC_SUBSIDY", "补贴发放",
        Arrays.asList("姓名", "证件号", "预拨月份", "补发月份", "补贴金额", "户籍地址", "户籍点委", 
            "联系电话", "残疾人角色", "来源", "开户银行", "支行名称", "开户人姓名", "银行账号")),
    
    PRECISE_HELP("precise_help", "精准帮扶", "T_IMP_PRECISE_HELP", "补贴发放",
        Arrays.asList("姓名", "身份证", "补贴金额", "是否访视", "开户人", "开户行", 
            "开户支行", "银行账号", "补贴原因", "补贴原因描述")),
    
    DISABLED_APPLICATION("disabled_application", "残疾人（人/团体申请）", "T_IMP_DISABLED_APPLICATION", "其他服务",
        Arrays.asList("姓名", "证件号", "户籍区县", "户籍街道", "残疾类别", "残疾等级", 
            "年龄", "事项名称", "推送时间", "意向")),
    
    ASSISTIVE_DEVICE("assistive_device", "辅助器具", "T_IMP_ASSISTIVE_DEVICE", "辅助服务",
        Arrays.asList("进度", "申请来源", "申请单号", "订单号", "姓名", "证件号", "户籍区县", "户籍街道", 
            "残疾类别", "残疾等级", "残疾部位", "评定结果", "收货人", "收货电话", "收货地址", 
            "申请日期", "审核日期", "领取日期", "申请状态", "申请通过", "户籍地址", 
            "补贴金额", "单位金额", "个人承担", "补贴金额")),
    
    HEALTH_CHECK("health_check", "体检", "T_IMP_HEALTH_CHECK", "医疗康复",
        Arrays.asList("姓名", "证件号", "户籍区县", "户籍街道", "残疾类别", "残疾等级", 
            "年龄", "体检机构", "体检日期")),
    
    TWO_SUBSIDIES("two_subsidies", "两项补贴", "T_IMP_TWO_SUBSIDIES", "补贴发放",
        Arrays.asList("姓名", "证件号", "户籍区县", "户籍街道", "残疾类别", "残疾等级", 
            "年龄", "申请时间", "补贴类型", "补贴标准", "补贴金额", "状态")),
    
    EMPLOYMENT("employment", "就业", "T_IMP_EMPLOYMENT", "就业培训",
        Arrays.asList("姓名", "性别", "残疾证号", "证件号", "残疾登记", "就业状况", "从事行业", 
            "就业途径", "未就业原因", "单位名称", "劳动合同", "固定期限合同", "启动时间", 
            "截止时间", "就业业态", "生活自理能力", "就业意愿", "户口所在地址", "户口所在街道", 
            "户口所在村/社区", "联系方式", "手机号", "电话号码", "录入地区")),
    
    VOCATIONAL_TRAINING("vocational_training", "职业技能培训", "T_IMP_VOCATIONAL_TRAINING", "就业培训",
        Arrays.asList("姓名", "残疾人证", "残疾类别", "残疾等级", "年龄", "户籍地", 
            "培训项目名称", "课程类别", "培训等级")),
    
    SUNSHINE_INSTITUTION("sunshine_institution", "阳光机构", "T_IMP_SUNSHINE_INSTITUTION", "其他服务",
        Arrays.asList("姓名", "身份证号", "阳光惠地", "注册日期", "有效状态",
            "区县", "街道", "机构名称", "性别", "年龄", "残疾类别", "残疾等级", 
            "学员出生年月", "月份")),
    
    BASIC_SURVEY("basic_survey", "残疾人基本状况调查", "T_IMP_BASIC_SURVEY", "调查统计",
        Arrays.asList("地址", "姓名", "残疾人证号", "婚姻状况", "联系人姓名", "和联系人关系", 
            "是否在养老/护理机构", "居住信息（家庭住址/房屋住址）", "家庭经济情况", "残疾状况", 
            "工作就业情况", "社会保障情况", "健康状况", "机构康复/辅助器具服务情况", 
            "文化体育情况", "家庭结构", "户主姓名", "户主身份证号")),
    
    MEILUN_CAR("meilun_car", "美伦车", "T_IMP_MEILUN_CAR", "其他服务",
        Arrays.asList("街镇", "性别", "身份证号", "残疾等级", "残疾人证", "户籍地", "居住地", 
            "邮政编码", "固话号码", "手机号码", "开户行", "银行卡账号", "申请时间", "备注", "补贴金额")),
    
    EDUCATION_AID("education_aid", "助学", "T_IMP_EDUCATION_AID", "其他服务",
        Arrays.asList("学生", "年龄", "性别", "残疾证号", "学校", "专业", "联系方式", "户籍地", "居住地")),
    
    FESTIVAL_CONDOLENCE("festival_condolence", "节日慰问金（回、国、困）", "T_IMP_FESTIVAL_CONDOLENCE", "补贴发放",
        Arrays.asList("大病困难", "残疾人姓名", "残疾类别", "残疾证号", "患病情况", "联系电话", 
            "居住地址", "金额", "居委", "现居住地址")),
    
    TEMPORARY_AID("temporary_aid", "临时困难金", "T_IMP_TEMPORARY_AID", "补贴发放",
        Arrays.asList("街镇", "收款人姓名", "残疾人证号", "收款人账号", "收款人开户行", 
            "收款人开户支行", "补贴金额")),
    
    PAIRING_HELP("pairing_help", "结对帮扶", "T_IMP_PAIRING_HELP", "帮扶服务",
        Arrays.asList("姓名", "性别", "户籍地", "身份证号", "家庭住址", "婚姻状况", "就业状况", 
            "家庭情况", "帮扶原因", "助学/助残/助医/助困", "人员类别", "本人经济情况", 
            "家庭经济情况", "其他问题及诉求"));

    private final String code;
    private final String name;
    private final String tableName;
    private final String category;
    private final List<String> fields;

    ImportModule(String code, String name, String tableName, String category, List<String> fields) {
        this.code = code;
        this.name = name;
        this.tableName = tableName;
        this.category = category;
        this.fields = fields;
    }

    public String getCode() { return code; }
    public String getName() { return name; }
    public String getTableName() { return tableName; }
    public String getCategory() { return category; }
    public List<String> getFields() { return fields; }

    public static ImportModule fromCode(String code) {
        for (ImportModule module : values()) {
            if (module.code.equals(code)) {
                return module;
            }
        }
        return null;
    }

    public static List<ImportModule> getByCategory(String category) {
        return Arrays.stream(values())
            .filter(m -> m.category.equals(category))
            .toList();
    }

    public static List<String> getAllCategories() {
        return Arrays.stream(values())
            .map(m -> m.category)
            .distinct()
            .toList();
    }
}
