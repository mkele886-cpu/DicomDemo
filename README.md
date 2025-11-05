# DicomDemo

A demonstration application implementing DICOM (Digital Imaging and Communications in Medicine) services using the fo-dicom library.

## Overview

This project demonstrates the implementation of four core DICOM services:

1. **DicomStorageService (C-STORE SCP)** - Receives and stores DICOM images from DICOM clients
2. **DicomQueryService (C-FIND SCP)** - Handles queries for DICOM studies, series, and images
3. **DicomMoveService (C-MOVE SCP)** - Handles requests to move DICOM images to a destination
4. **DicomVerificationService (C-ECHO SCP)** - Responds to verification requests to test DICOM connectivity

## Features

- **Storage Service**: Automatically organizes received DICOM files by Study Instance UID
- **Query Service**: Provides demo responses for C-FIND requests
- **Move Service**: Simulates C-MOVE operations for image transfer
- **Verification Service**: Simple echo service to verify connectivity

## Service Ports

The application runs the following services on these ports:

- Storage Service (C-STORE): Port **11112**
- Query Service (C-FIND): Port **11113**
- Move Service (C-MOVE): Port **11114**
- Verification Service (C-ECHO): Port **11115**

Application Entity Title: **DICOMDEMO**

## Prerequisites

- .NET 8.0 SDK or later

## Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/mkele886-cpu/DicomDemo.git
   cd DicomDemo
   ```

2. Restore dependencies:
   ```bash
   cd DicomDemo
   dotnet restore
   ```

3. Build the project:
   ```bash
   dotnet build
   ```

## Usage

### Running the Application

To start all DICOM services:

```bash
dotnet run
```

The application will start all four services and display their port numbers. Press `Ctrl+C` to stop all services.

### Testing with DICOM Tools

You can test the services using standard DICOM tools such as:

#### C-ECHO (Verification)
```bash
echoscu -aet TESTAE -aec DICOMDEMO localhost 11115
```

#### C-STORE (Storage)
```bash
storescu -aet TESTAE -aec DICOMDEMO localhost 11112 <dicom-file.dcm>
```

#### C-FIND (Query)
```bash
findscu -aet TESTAE -aec DICOMDEMO -P -k PatientName="*" localhost 11113
```

#### C-MOVE (Move)
```bash
movescu -aet TESTAE -aec DICOMDEMO -d DESTINATIONAE -k StudyInstanceUID="1.2.3.4.5" localhost 11114
```

## Storage Location

DICOM files received via C-STORE are saved in the `DicomStorage` directory within the application's working directory, organized by Study Instance UID.

## Project Structure

```
DicomDemo/
├── Services/
│   ├── DicomStorageService.cs       # C-STORE implementation
│   ├── DicomQueryService.cs         # C-FIND implementation
│   ├── DicomMoveService.cs          # C-MOVE implementation
│   └── DicomVerificationService.cs  # C-ECHO implementation
├── Program.cs                        # Application entry point
└── DicomDemo.csproj                  # Project file
```

## Dependencies

- [fo-dicom](https://github.com/fo-dicom/fo-dicom) v5.2.4 - DICOM library for .NET
- Microsoft.Extensions.Logging v8.0.1 - Logging framework
- Microsoft.Extensions.Logging.Console v9.0.10 - Console logging provider

## Notes

- This is a demonstration application intended for educational purposes
- In a production environment, you would need to:
  - Implement proper database storage for DICOM metadata
  - Add authentication and authorization
  - Implement proper error handling and validation
  - Add support for all required DICOM transfer syntaxes
  - Implement actual C-MOVE functionality with real image transfers

## License

This project is provided as-is for demonstration purposes.