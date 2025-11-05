package com.yinglian.pacs;

import org.dcm4che3.net.ApplicationEntity;
import org.dcm4che3.net.Connection;
import org.dcm4che3.net.Device;
import org.dcm4che3.net.service.DicomServiceRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * @author Github Copilot
 * @since 2025-11-05
 * PACS服务器主类
 * 用于启动和管理DICOM服务，包括存储、查询、移动和验证
 */
public class PacsServer {

    private static final Logger LOG = LoggerFactory.getLogger(PacsServer.class);
    private static final String AE_TITLE = "PACS_SERVER";
    private static final int PORT = 11112;
    private static final File STORAGE_DIR = new File("pacs-storage");

    private final Device device = new Device("pacs-server");
    private final ApplicationEntity ae = new ApplicationEntity(AE_TITLE);
    private final Connection conn = new Connection();

    /**
     * 构造函数，初始化服务器
     *
     * @throws IOException 如果发生I/O错误
     */
    public PacsServer() throws IOException {
        // 配置设备和AE
        device.addConnection(conn);
        device.addApplicationEntity(ae);
        ae.setAETitle(AE_TITLE);
        ae.addConnection(conn);

        // 注册DICOM服务
        DicomServiceRegistry serviceRegistry = new DicomServiceRegistry();
        serviceRegistry.addDicomService(new DicomStorageService(STORAGE_DIR));
        serviceRegistry.addDicomService(new DicomQueryService(STORAGE_DIR));
        serviceRegistry.addDicomService(new DicomMoveService()); // Updated to use the no-argument constructor
        serviceRegistry.addDicomService(new DicomVerificationService());
        ae.setDimseRQHandler(serviceRegistry);

        // 配置连接
        conn.setPort(PORT);
        conn.setHostname("0.0.0.0");
    }

    /**
     * 启动服务器
     *
     * @throws IOException              如果启动时发生I/O错误
     * @throws GeneralSecurityException 如果发生安全错误
     */
    public void start() throws IOException, GeneralSecurityException {
        // 创建线程池
        ExecutorService executorService = Executors.newCachedThreadPool();
        ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        device.setExecutor(executorService);
        device.setScheduledExecutor(scheduledExecutorService);

        // 启动设备
        device.bindConnections();
        LOG.info("PACS Server started on port {} with AE Title {}", PORT, AE_TITLE);
    }

    /**
     * 停止服务器
     */
    public void stop() {
        device.unbindConnections();
        LOG.info("PACS Server stopped.");
    }

    /**
     * 主方法，创建并启动PACS服务器
     *
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        try {
            PacsServer pacsServer = new PacsServer();
            pacsServer.start();

            // 使服务器保持运行
            Runtime.getRuntime().addShutdownHook(new Thread(pacsServer::stop));
        } catch (IOException | GeneralSecurityException e) {
            LOG.error("Failed to start PACS server", e);
        }
    }
}
