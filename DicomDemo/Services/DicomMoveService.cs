using System.Text;
using FellowOakDicom;
using FellowOakDicom.Network;
using FellowOakDicom.Network.Client;
using Microsoft.Extensions.Logging;

namespace DicomDemo.Services;

/// <summary>
/// DICOM Move Service (C-MOVE SCP)
/// Handles requests to move DICOM images to a destination
/// </summary>
public class DicomMoveService : DicomService, IDicomServiceProvider, IDicomCMoveProvider
{
    private readonly ILogger<DicomMoveService> _logger;

    public DicomMoveService(INetworkStream stream, Encoding fallbackEncoding, ILogger logger,
        DicomServiceDependencies dependencies)
        : base(stream, fallbackEncoding, logger, dependencies)
    {
        _logger = logger as ILogger<DicomMoveService> ?? 
                  LoggerFactory.Create(builder => builder.AddConsole()).CreateLogger<DicomMoveService>();
    }

    public void OnReceiveAbort(DicomAbortSource source, DicomAbortReason reason)
    {
        _logger.LogWarning($"C-MOVE: Abort received - Source: {source}, Reason: {reason}");
    }

    public void OnConnectionClosed(Exception exception)
    {
        if (exception != null)
        {
            _logger.LogError(exception, "C-MOVE: Connection closed with error");
        }
        else
        {
            _logger.LogInformation("C-MOVE: Connection closed");
        }
    }

    public async Task OnReceiveAssociationRequestAsync(DicomAssociation association)
    {
        _logger.LogInformation($"C-MOVE: Association request received from {association.CallingAE} to {association.CalledAE}");

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
        _logger.LogInformation("C-MOVE: Association release requested");
    }

    public async IAsyncEnumerable<DicomCMoveResponse> OnCMoveRequestAsync(DicomCMoveRequest request)
    {
        var destination = request.DestinationAE;
        _logger.LogInformation($"C-MOVE: Move request received - Destination: {destination}");

        // Log the query parameters
        if (request.Dataset.Contains(DicomTag.StudyInstanceUID))
        {
            var studyUid = request.Dataset.GetSingleValueOrDefault(DicomTag.StudyInstanceUID, string.Empty);
            _logger.LogInformation($"C-MOVE: Study UID: {studyUid}");
        }

        if (request.Dataset.Contains(DicomTag.PatientID))
        {
            var patientId = request.Dataset.GetSingleValueOrDefault(DicomTag.PatientID, string.Empty);
            _logger.LogInformation($"C-MOVE: Patient ID: {patientId}");
        }

        // For demo purposes, simulate a successful move operation
        // In a real implementation, this would:
        // 1. Find the matching images
        // 2. Establish a connection to the destination AE
        // 3. Send the images using C-STORE
        // 4. Return progress updates

        _logger.LogInformation("C-MOVE: Simulating image transfer (no actual images to move in demo)");

        // Return pending status (would be sent during actual transfer)
        var pendingResponse = new DicomCMoveResponse(request, DicomStatus.Pending);
        pendingResponse.Dataset.AddOrUpdate(DicomTag.NumberOfRemainingSuboperations, 0);
        pendingResponse.Dataset.AddOrUpdate(DicomTag.NumberOfCompletedSuboperations, 1);
        pendingResponse.Dataset.AddOrUpdate(DicomTag.NumberOfFailedSuboperations, 0);
        pendingResponse.Dataset.AddOrUpdate(DicomTag.NumberOfWarningSuboperations, 0);
        yield return pendingResponse;

        // Final success response
        _logger.LogInformation("C-MOVE: Move operation completed");
        yield return new DicomCMoveResponse(request, DicomStatus.Success);

        await Task.CompletedTask;
    }
}
