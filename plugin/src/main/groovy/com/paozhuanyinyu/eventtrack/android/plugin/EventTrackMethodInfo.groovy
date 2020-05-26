package com.paozhuanyinyu.eventtrack.android.plugin

class EventTrackMethodInfo{
    /**
     * 原方法名
     */
    String name
    /**
     * 原方法描述
     */
    String desc
    /**
     * 方法所在的接口或类
     */
    String parent
    /**
     * 采集数据的方法名
     */
    String agentName
    /**
     * 采集数据的方法描述
     */
    String agentDesc
    /**
     * 采集数据的方法起始索引
     */
    int paramsStart
    int paramCount
    List<Integer> opcodes

    EventTrackMethodInfo(String name, String desc, String parent, String agentName, String agentDesc, int paramsStart, int paramCount, List<Integer> opcodes) {
        this.name = name
        this.desc = desc
        this.parent = parent
        this.agentName = agentName
        this.agentDesc = agentDesc
        this.paramsStart = paramsStart
        this.paramCount = paramCount
        this.opcodes = opcodes
    }

    EventTrackMethodInfo(String name, String desc, String agentName) {
        this.name = name
        this.desc = desc
        this.agentName = agentName
    }
}