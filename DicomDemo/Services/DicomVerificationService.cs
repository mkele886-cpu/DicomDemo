using System.Text;
using FellowOakDicom;
using FellowOakDicom.Network;
using Microsoft.Extensions.Logging;

namespace DicomDemo.Services;

/// <summary>
/// DICOM Verification Service (C-ECHO SCP)
/// Responds to verification requests to test DICOM connectivity
/// </summary>
public class DicomVerificationService : DicomService, IDicomServiceProvider, IDicomCEchoProvider
{
    private readonly ILogger<DicomVerificationService> _logger;

    public DicomVerificationService(INetworkStream stream, Encoding fallbackEncoding, ILogger logger,
        DicomServiceDependencies dependencies)
        : base(stream, fallbackEncoding, logger, dependencies)
    {
        _logger = logger as ILogger<DicomVerificationService> ?? 
                  LoggerFactory.Create(builder => builder.AddConsole()).CreateLogger<DicomVerificationService>();
    }

    public void OnReceiveAbort(DicomAbortSource source, DicomAbortReason reason)
    {
        _logger.LogWarning($"C-ECHO: Abort received - Source: {source}, Reason: {reason}");
    }

    public void OnConnectionClosed(Exception exception)
    {
        if (exception != null)
        {
            _logger.LogError(exception, "C-ECHO: Connection closed with error");
        }
        else
        {
            _logger.LogInformation("C-ECHO: Connection closed");
        }
    }

    public async Task OnReceiveAssociationRequestAsync(DicomAssociation association)
    {
        _logger.LogInformation($"C-ECHO: Association request received from {association.CallingAE} to {association.CalledAE}");

        // Accept all presentation contexts (typically only Verification SOP Class)
        foreach (var pc in association.PresentationContexts)
        {
            pc.SetResult(DicomPresentationContextResult.Accept);
        }

        await SendAssociationAcceptAsync(association);
    }

    public async Task OnReceiveAssociationReleaseRequestAsync()
    {
        await SendAssociationReleaseResponseAsync();
        _logger.LogInformation("C-ECHO: Association release requested");
    }

    public Task<DicomCEchoResponse> OnCEchoRequestAsync(DicomCEchoRequest request)
    {
        _logger.LogInformation("C-ECHO: Echo request received - responding with Success");
        return Task.FromResult(new DicomCEchoResponse(request, DicomStatus.Success));
    }
}
