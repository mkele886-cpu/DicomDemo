package com.kele.pacs;

import org.dcm4che3.net.ApplicationEntity;
import org.dcm4che3.net.Connection;
import org.dcm4che3.net.Device;

/**
 * @author Github Copilot
 * @since 2025-11-05
 * DICOM服务器配置
 * 用于配置和管理DICOM网络通信所需的设备和应用程序实体（AE）
 */
public class DicomConfig {

    private final Device device = new Device("pacs-server");
    private final ApplicationEntity ae = new ApplicationEntity("PACS_SERVER");

    /**
     * 构造函数，初始化并配置设备和应用程序实体
     */
    public DicomConfig() {
        // 将AE添加到设备中
        device.addApplicationEntity(ae);
        // 添加一个网络连接
        device.addConnection(new Connection());
        // 设置AE的标题
        ae.setAETitle("PACS_SERVER");
        // 接受所有类型的传输能力
        //ae.setAcceptor(true);
        // 添加所有支持的传输能力
        ae.addTransferCapability(new org.dcm4che3.net.TransferCapability(
                "*", "*", org.dcm4che3.net.TransferCapability.Role.SCP, "*"));
    }

    /**
     * 获取设备实例aa
     *
     * @return Device对象
     */
    public Device getDevice() {
        return device;
    }

    /**
     * 获取应用程序实体实例
     *
     * @return ApplicationEntity对象
     */
    public ApplicationEntity getAe() {
        return ae;
    }
}

