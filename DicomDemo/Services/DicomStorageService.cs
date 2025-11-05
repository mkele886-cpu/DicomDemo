using System.Text;
using FellowOakDicom;
using FellowOakDicom.Network;
using Microsoft.Extensions.Logging;

namespace DicomDemo.Services;

/// <summary>
/// DICOM Storage Service (C-STORE SCP)
/// Receives and stores DICOM images from DICOM clients
/// </summary>
public class DicomStorageService : DicomService, IDicomServiceProvider, IDicomCStoreProvider
{
    private readonly ILogger<DicomStorageService> _logger;
    private static readonly string StoragePath = Path.Combine(Directory.GetCurrentDirectory(), "DicomStorage");

    public DicomStorageService(INetworkStream stream, Encoding fallbackEncoding, ILogger logger,
        DicomServiceDependencies dependencies)
        : base(stream, fallbackEncoding, logger, dependencies)
    {
        _logger = logger as ILogger<DicomStorageService> ?? 
                  LoggerFactory.Create(builder => builder.AddConsole()).CreateLogger<DicomStorageService>();
        
        // Ensure storage directory exists
        Directory.CreateDirectory(StoragePath);
    }

    public void OnReceiveAbort(DicomAbortSource source, DicomAbortReason reason)
    {
        _logger.LogWarning($"C-STORE: Abort received - Source: {source}, Reason: {reason}");
    }

    public void OnConnectionClosed(Exception exception)
    {
        if (exception != null)
        {
            _logger.LogError(exception, "C-STORE: Connection closed with error");
        }
        else
        {
            _logger.LogInformation("C-STORE: Connection closed");
        }
    }

    public async Task OnReceiveAssociationRequestAsync(DicomAssociation association)
    {
        _logger.LogInformation($"C-STORE: Association request received from {association.CallingAE} to {association.CalledAE}");

        // Accept all presentation contexts
        foreach (var pc in association.PresentationContexts)
        {
            pc.SetResult(DicomPresentationContextResult.Accept);
        }

        await SendAssociationAcceptAsync(association);
    }

    public async Task OnReceiveAssociationReleaseRequestAsync()
    {
        await SendAssociationReleaseResponseAsync();
        _logger.LogInformation("C-STORE: Association release requested");
    }

    public async Task<DicomCStoreResponse> OnCStoreRequestAsync(DicomCStoreRequest request)
    {
        var studyUid = request.Dataset.GetSingleValue<string>(DicomTag.StudyInstanceUID);
        var sopInstanceUid = request.SOPInstanceUID.UID;

        _logger.LogInformation($"C-STORE: Receiving image - SOP Instance UID: {sopInstanceUid}");

        try
        {
            // Create directory structure by Study UID
            var studyPath = Path.Combine(StoragePath, studyUid);
            Directory.CreateDirectory(studyPath);

            // Save the DICOM file
            var fileName = Path.Combine(studyPath, $"{sopInstanceUid}.dcm");
            await request.File.SaveAsync(fileName);

            _logger.LogInformation($"C-STORE: Image saved successfully to {fileName}");
            return new DicomCStoreResponse(request, DicomStatus.Success);
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, $"C-STORE: Error saving image {sopInstanceUid}");
            return new DicomCStoreResponse(request, DicomStatus.ProcessingFailure);
        }
    }

    public Task OnCStoreRequestExceptionAsync(string tempFileName, Exception e)
    {
        _logger.LogError(e, $"C-STORE: Exception during storage of {tempFileName}");
        return Task.CompletedTask;
    }
}
