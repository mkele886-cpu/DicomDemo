using FellowOakDicom;
using FellowOakDicom.Network;
using DicomDemo.Services;
using Microsoft.Extensions.Logging;

namespace DicomDemo;

class Program
{
    private const int StoragePort = 11112;
    private const int QueryPort = 11113;
    private const int MovePort = 11114;
    private const int VerificationPort = 11115;
    private const string AETitle = "DICOMDEMO";

    static async Task Main(string[] args)
    {
        Console.WriteLine("===========================================");
        Console.WriteLine("         DICOM Demo Application");
        Console.WriteLine("===========================================");
        Console.WriteLine();

        // Setup logging
        var loggerFactory = LoggerFactory.Create(builder =>
        {
            builder.AddConsole();
            builder.SetMinimumLevel(LogLevel.Information);
        });

        try
        {
            // Create DICOM servers for each service
            var storageServer = DicomServerFactory.Create<DicomStorageService>(StoragePort);
            var queryServer = DicomServerFactory.Create<DicomQueryService>(QueryPort);
            var moveServer = DicomServerFactory.Create<DicomMoveService>(MovePort);
            var verificationServer = DicomServerFactory.Create<DicomVerificationService>(VerificationPort);

            Console.WriteLine("DICOM Services Started:");
            Console.WriteLine($"  - Storage Service (C-STORE):       Port {StoragePort}");
            Console.WriteLine($"  - Query Service (C-FIND):          Port {QueryPort}");
            Console.WriteLine($"  - Move Service (C-MOVE):           Port {MovePort}");
            Console.WriteLine($"  - Verification Service (C-ECHO):   Port {VerificationPort}");
            Console.WriteLine($"  - Application Entity Title:        {AETitle}");
            Console.WriteLine();
            Console.WriteLine("Services are ready to accept connections.");
            Console.WriteLine("Press Ctrl+C to stop all services.");
            Console.WriteLine();

            // Keep the application running
            await Task.Delay(Timeout.Infinite);
        }
        catch (Exception ex)
        {
            Console.WriteLine($"Error starting DICOM services: {ex.Message}");
            Console.WriteLine(ex.StackTrace);
        }
    }
}
