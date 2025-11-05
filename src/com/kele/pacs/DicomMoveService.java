package com.kele.pacs;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.UID;
import org.dcm4che3.net.Association;
import org.dcm4che3.net.pdu.PresentationContext;
import org.dcm4che3.net.service.BasicCMoveSCP;
import org.dcm4che3.net.service.DicomServiceException;
import org.dcm4che3.net.service.RetrieveTask; // 核心类
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DICOM移动服务
 * 实现了C-MOVE SCP功能，用于将DICOM图像从一个位置移动到另一个位置
 */
public class DicomMoveService extends BasicCMoveSCP {

    private static final Logger LOG = LoggerFactory.getLogger(DicomMoveService.class);

    /**
     * 构造函数
     */
    public DicomMoveService() {
        // C-MOVE 通常使用 Study Root Query/Retrieve Model
        super(UID.StudyRootQueryRetrieveInformationModelMove);
    }

    /**
     * 核心方法：解析请求，并创建执行移动操作的任务。
     *
     * @param as 与客户端的关联
     * @param pc 表示上下文
     * @param rq 请求的属性 (C-MOVE-RQ 命令)
     * @param keys 键属性 (用于查询要移动的影像)
     * @return RetrieveTask 负责执行实际的 C-STORE 子操作
     * @throws DicomServiceException 如果处理过程中发生错误
     */
    @Override
    protected RetrieveTask calculateMatches(Association as, PresentationContext pc, Attributes rq, Attributes keys)
            throws DicomServiceException {

        // 1. 获取移动目标AE Title (Move Destination)
        String moveDestination = rq.getString(Tag.MoveDestination);
        if (moveDestination == null || moveDestination.isEmpty()) {
            // 如果缺少移动目标，返回错误状态
            throw new DicomServiceException(org.dcm4che3.net.Status.MoveDestinationUnknown,
                    "Move Destination (0000,0001) is missing or unknown.");
        }

        // 2. 解析查询键 (Keys) 确定要移动哪些影像
        String studyUID = keys.getString(Tag.StudyInstanceUID);
        LOG.info("收到C-MOVE请求，目标: {}，待移动 StudyInstanceUID: {}", moveDestination, studyUID);

        // 3. 实际的业务逻辑：查询数据库，找到匹配的SOP实例列表
        // 伪代码: List<Attributes> matches = Database.queryInstances(keys);

        // 4. 返回一个 RetrieveTask 实例来执行 C-MOVE 操作
        // 这是一个抽象过程，实际代码中需要您创建一个实现类，如 MyMoveTask

        // 示例：返回 null 相当于 BasicCMoveSCP 的默认行为，即返回成功（0个匹配项）
        // 实际应用中，您需要返回一个自定义的 RetrieveTask 对象。
        return null;
    }
}