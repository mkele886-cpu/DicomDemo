package com.yinglian.pacs;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.net.Association;
import org.dcm4che3.net.Status;
import org.dcm4che3.net.pdu.PresentationContext;
import org.dcm4che3.net.service.BasicCFindSCP;
import org.dcm4che3.net.service.DicomServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Gemini
 * @since 2025-11-05
 * DICOM查询服务
 * 实现了C-FIND SCP功能
 */
public class DicomQueryService extends BasicCFindSCP {

    private static final Logger LOG = LoggerFactory.getLogger(DicomQueryService.class);
    private final File storageDir;

    // 手动定义 StudyRootQueryRetrieveInformationModelFIND 的 UID
    private static final String STUDY_ROOT_QUERY_RETRIEVE_INFORMATION_MODEL_FIND = "1.2.840.10008.5.1.4.1.2.2.1";

    /**
     * 构造函数
     *
     * @param storageDir 存储目录
     */
    public DicomQueryService(File storageDir) {
        // 使用手动定义的 UID
        super(STUDY_ROOT_QUERY_RETRIEVE_INFORMATION_MODEL_FIND);
        this.storageDir = storageDir;
    }

    /**
     * C-FIND服务的核心方法，处理查询请求
     *
     * @param as      与客户端的关联
     * @param pc      表示上下文
     * @param rq      请求的属性 (包含查询键)
     * @param rsp     响应命令属性
     * @throws DicomServiceException 如果处理过程中发生错误
     */
    protected void doFind(Association as, PresentationContext pc, Attributes rq, Attributes rsp)
            throws DicomServiceException {

        try {
            // 查找匹配的文件
            List<Attributes> result = findMatchingFiles(rq);

            // 遍历所有匹配结果，并发送 C-FIND 响应
            for (Attributes attrs : result) {
                as.tryWriteDimseRSP(pc, rsp, attrs);
            }
        } catch (IOException e) {
            LOG.error("Failed to process C-FIND request", e);
            throw new DicomServiceException(Status.ProcessingFailure, e);
        }
    }

    /**
     * 在存储目录中查找匹配的DICOM文件
     * (逻辑保持不变，但需注意性能问题，见下方说明)
     */
    private List<Attributes> findMatchingFiles(Attributes queryAttrs) throws IOException {
        List<Attributes> result = new ArrayList<>();
        File[] files = storageDir.listFiles();
        if (files == null) {
            return result;
        }

        for (File file : files) {
            if (file.isFile()) {
                // 注意: 这种做法在大规模PACS中效率极低，仅用于示例
                try (DicomInputStream dis = new DicomInputStream(file)) {
                    Attributes fileAttrs = dis.readDataset(-1, -1);
                    if (matches(queryAttrs, fileAttrs)) {
                        result.add(createResponseAttributes(queryAttrs, fileAttrs));
                    }
                }
            }
        }
        return result;
    }

    // matches 和 createResponseAttributes 方法保持不变

    /**
     * 检查文件属性是否与查询条件匹配
     */
    private boolean matches(Attributes queryAttrs, Attributes fileAttrs) {
        // ... (保持原逻辑)
        for (int tag : queryAttrs.tags()) {
            // ... (检查逻辑)
            if (queryAttrs.getString(tag, "").isEmpty()) {
                continue;
            }
            if (!fileAttrs.contains(tag) || !queryAttrs.getString(tag).equals(fileAttrs.getString(tag))) {
                return false;
            }
        }
        return true;
    }

    private Attributes createResponseAttributes(Attributes queryAttrs, Attributes fileAttrs) throws IOException {
        Attributes response = new Attributes();
        for (int tag : queryAttrs.tags()) {
            if (fileAttrs.contains(tag)) {
                response.setValue(tag, fileAttrs.getVR(tag), fileAttrs.getBytes(tag));
            }
        }
        // 确保 C-FIND 响应所需的关键 UID 在列
        if (!response.contains(Tag.SOPClassUID)) {
            response.setString(Tag.SOPClassUID, fileAttrs.getVR(Tag.SOPClassUID), fileAttrs.getString(Tag.SOPClassUID));
        }
        if (!response.contains(Tag.SOPInstanceUID)) {
            response.setString(Tag.SOPInstanceUID, fileAttrs.getVR(Tag.SOPInstanceUID), fileAttrs.getString(Tag.SOPInstanceUID));
        }
        return response;
    }
}