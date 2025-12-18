# verify_api.ps1

function Test-Endpoint {
    param([string]$Url, [string]$Description, [int]$ExpectedStatus)

    Write-Host " Testing $Description ($Url)..." -NoNewline
    try {
        $response = Invoke-WebRequest -Uri $Url -Method Get -ErrorAction Stop -UseBasicParsing
        $status = $response.StatusCode
        $content = $response.Content
    }
    catch {
        $response = $_.Exception.Response
        if ($null -ne $response) {
            $status = [int]$response.StatusCode
            # Read the stream for error content
            $stream = $response.GetResponseStream()
            $reader = New-Object System.IO.StreamReader($stream)
            $content = $reader.ReadToEnd()
        }
        else {
            $status = 0
            $content = $_.Exception.Message
        }
    }

    if ($status -eq $ExpectedStatus) {
        Write-Host " [SUCCESS] Got $status" -ForegroundColor Green
        if ($status -eq 200) {
            Write-Host "   Response (First 100 chars): $($content.Substring(0, [Math]::Min($content.Length, 100)))..." -ForegroundColor Gray
        }
        elseif ($status -eq 404) {
            # Check if it is our JSON
            if ($content -match "message") {
                Write-Host "   [VERIFIED] JSON Error Response received." -ForegroundColor Green
            }
            else {
                Write-Host "   [WARNING] 404 received but might not be JSON." -ForegroundColor Yellow
            }
            Write-Host "   Response: $content" -ForegroundColor Gray
        }
    }
    else {
        Write-Host " [FAILURE] Expected $ExpectedStatus, got $status" -ForegroundColor Red
        Write-Host "   Response: $content" -ForegroundColor Gray
    }
    Write-Host ""
}

Write-Host "=== API Verification Script (Security DISABLED) ==="
Test-Endpoint -Url "http://localhost:8080/" -Description "Root Endpoint" -ExpectedStatus 200
Test-Endpoint -Url "http://localhost:8080/missing-random-path" -Description "Non-Existent Path" -ExpectedStatus 404
