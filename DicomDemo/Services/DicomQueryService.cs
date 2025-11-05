using System.Text;
using FellowOakDicom;
using FellowOakDicom.Network;
using Microsoft.Extensions.Logging;

namespace DicomDemo.Services;

/// <summary>
/// DICOM Query Service (C-FIND SCP)
/// Handles queries for DICOM studies, series, and images
/// </summary>
public class DicomQueryService : DicomService, IDicomServiceProvider, IDicomCFindProvider
{
    private readonly ILogger<DicomQueryService> _logger;

    public DicomQueryService(INetworkStream stream, Encoding fallbackEncoding, ILogger logger,
        DicomServiceDependencies dependencies)
        : base(stream, fallbackEncoding, logger, dependencies)
    {
        _logger = logger as ILogger<DicomQueryService> ?? 
                  LoggerFactory.Create(builder => builder.AddConsole()).CreateLogger<DicomQueryService>();
    }

    public void OnReceiveAbort(DicomAbortSource source, DicomAbortReason reason)
    {
        _logger.LogWarning($"C-FIND: Abort received - Source: {source}, Reason: {reason}");
    }

    public void OnConnectionClosed(Exception exception)
    {
        if (exception != null)
        {
            _logger.LogError(exception, "C-FIND: Connection closed with error");
        }
        else
        {
            _logger.LogInformation("C-FIND: Connection closed");
        }
    }

    public async Task OnReceiveAssociationRequestAsync(DicomAssociation association)
    {
        _logger.LogInformation($"C-FIND: Association request received from {association.CallingAE} to {association.CalledAE}");

        // Accept all presentation contexts for Query/Retrieve
        foreach (var pc in association.PresentationContexts)
        {
            pc.SetResult(DicomPresentationContextResult.Accept);
        }

        await SendAssociationAcceptAsync(association);
    }

    public async Task OnReceiveAssociationReleaseRequestAsync()
    {
        await SendAssociationReleaseResponseAsync();
        _logger.LogInformation("C-FIND: Association release requested");
    }

    public async IAsyncEnumerable<DicomCFindResponse> OnCFindRequestAsync(DicomCFindRequest request)
    {
        var level = request.Level;
        _logger.LogInformation($"C-FIND: Query request received at {level} level");

        // Log query parameters
        if (request.Dataset.Contains(DicomTag.PatientName))
        {
            var patientName = request.Dataset.GetSingleValueOrDefault(DicomTag.PatientName, string.Empty);
            _logger.LogInformation($"C-FIND: Searching for Patient Name: {patientName}");
        }

        if (request.Dataset.Contains(DicomTag.StudyInstanceUID))
        {
            var studyUid = request.Dataset.GetSingleValueOrDefault(DicomTag.StudyInstanceUID, string.Empty);
            _logger.LogInformation($"C-FIND: Searching for Study UID: {studyUid}");
        }

        // For demo purposes, return a sample response
        // In a real implementation, this would query a database
        var response = new DicomDataset
        {
            { DicomTag.PatientName, "Demo^Patient" },
            { DicomTag.PatientID, "DEMO001" },
            { DicomTag.StudyInstanceUID, "1.2.3.4.5.6.7.8.9" },
            { DicomTag.StudyDate, DateTime.Now.ToString("yyyyMMdd") },
            { DicomTag.StudyDescription, "Demo Study" },
            { DicomTag.AccessionNumber, "ACC001" }
        };

        _logger.LogInformation("C-FIND: Returning demo result");
        yield return new DicomCFindResponse(request, DicomStatus.Pending) { Dataset = response };

        // Final response indicating completion
        _logger.LogInformation("C-FIND: Query completed");
        yield return new DicomCFindResponse(request, DicomStatus.Success);

        await Task.CompletedTask;
    }
}
