package com.kele.pacs;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.net.Association;
import org.dcm4che3.net.Dimse;
import org.dcm4che3.net.pdu.PresentationContext;
import org.dcm4che3.net.service.BasicCEchoSCP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * DICOM验证服务 (重写 onDimseRQ 来添加日志)
 */
public class DicomVerificationService extends BasicCEchoSCP {

    private static final Logger LOG = LoggerFactory.getLogger(DicomVerificationService.class);

    public DicomVerificationService() {
    }

    /**
     * 重写 onDimseRQ 来拦截所有 DICOM 请求，并添加日志。
     *
     * @param as     与客户端的关联
     * @param pc     表示上下文
     * @param dimse  DICOM 消息类型 (应为 C_ECHO_RQ)
     * @param cmd    命令属性
     * @param keys   键属性 (C-ECHO 中通常为空)
     */
    @Override
    public void onDimseRQ(Association as, PresentationContext pc, Dimse dimse, Attributes cmd, Attributes keys)
            throws IOException {

        if (dimse == Dimse.C_ECHO_RQ) {

            // ✅ 修复点：使用 as.getSocket() 来获取远程客户端的 IP 地址
            String remoteIP = as.getSocket().getInetAddress().getHostAddress();

            LOG.info("Received C-ECHO request from: {} on connection: {}",
                    as.getRemoteAET(), remoteIP);
        }

        // 调用父类方法，让 BasicCEchoSCP 处理发送成功响应的逻辑
        // 必须调用 super.onDimseRQ，否则 C-ECHO 响应将不会发送。
        super.onDimseRQ(as, pc, dimse, cmd, keys);
    }
}