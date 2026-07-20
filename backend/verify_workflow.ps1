# Powershell script to verify the entire Decision workflow end-to-end via REST HTTP requests

$ErrorActionPreference = "Stop"

$ticks = [DateTime]::Now.Ticks
$username = "produser_$ticks"
$email = "$username@gmail.com"
$password = "Password123"

Write-Host "=============================================="
Write-Host "STARTING REST API END-TO-END FLOW VERIFICATION"
Write-Host "=============================================="
Write-Host "User: $username / $email"

# Helper for psql
function Execute-Sql ($query) {
    return psql -U postgres -d decision_hub -c "$query" 2>&1
}

# 1. REGISTER
Write-Host "`n[1] Registering User..."
$registerBody = @{
    username = $username
    email = $email
    password = $password
} | ConvertTo-Json

try {
    $registerResponse = Invoke-WebRequest -Uri "http://localhost:8080/api/auth/register" -Method Post -Body $registerBody -ContentType "application/json" -UseBasicParsing
    Write-Host "HTTP Status: $($registerResponse.StatusCode)"
    Write-Host "Response Content: $($registerResponse.Content)"
} catch {
    Write-Host "Failed register - $($_.Exception.Message)"
    exit 1
}

# 2. LOGIN
Write-Host "`n[2] Logging in..."
$loginBody = @{
    email = $email
    password = $password
} | ConvertTo-Json

try {
    $loginResponse = Invoke-WebRequest -Uri "http://localhost:8080/api/auth/login" -Method Post -Body $loginBody -ContentType "application/json" -UseBasicParsing
    Write-Host "HTTP Status: $($loginResponse.StatusCode)"
    Write-Host "Response Content: $($loginResponse.Content)"
    $loginJson = $loginResponse.Content | ConvertFrom-Json
    $token = $loginJson.token
} catch {
    Write-Host "Failed login - $($_.Exception.Message)"
    exit 1
}

$headers = @{
    Authorization = "Bearer $token"
}

# 3. CREATE COMMUNITY
Write-Host "`n[3] Creating Community..."
$communityBody = @{
    name = "Productivity Hub $ticks"
    slug = "productivity-hub-$ticks"
    description = "A community for testing productivity"
    categoryId = 1
    visibility = "PUBLIC"
} | ConvertTo-Json

try {
    $communityResponse = Invoke-WebRequest -Uri "http://localhost:8080/api/communities" -Method Post -Body $communityBody -ContentType "application/json" -Headers $headers -UseBasicParsing
    Write-Host "HTTP Status: $($communityResponse.StatusCode)"
    Write-Host "Response Content: $($communityResponse.Content)"
    $communityJson = $communityResponse.Content | ConvertFrom-Json
    $communityId = $communityJson.id
} catch {
    Write-Host "Failed community creation - $($_.Exception.Message)"
    exit 1
}

# Verify Community exists in DB
Write-Host "`n[3.1] Verifying Community in DB..."
Execute-Sql "SELECT id, name, slug, category_id, owner_id FROM communities WHERE id = $communityId"

# 4. CREATE DECISION
Write-Host "`n[4] Creating Decision..."
# Format date to ISO String
$endTime = [DateTime]::Now.AddDays(2).ToString("yyyy-MM-ddTHH:mm:ss")
$deadlineTime = [DateTime]::Now.AddDays(3).ToString("yyyy-MM-ddTHH:mm:ss")

$decisionBody = @{
    title = "Monitor choice for developer $ticks"
    description = "Which monitor is best for programming?"
    communityId = $communityId
    isPublic = $true
    votingType = "RATING_BASED"
    votingEndTime = $endTime
    deadline = $deadlineTime
    options = @()
    factors = @()
} | ConvertTo-Json

try {
    $decisionResponse = Invoke-WebRequest -Uri "http://localhost:8080/api/decisions" -Method Post -Body $decisionBody -ContentType "application/json" -Headers $headers -UseBasicParsing
    Write-Host "HTTP Status: $($decisionResponse.StatusCode)"
    Write-Host "Response Content: $($decisionResponse.Content)"
    $decisionJson = $decisionResponse.Content | ConvertFrom-Json
    $decisionId = $decisionJson.id
} catch {
    Write-Host "Failed decision creation - $($_.Exception.Message)"
    exit 1
}

# Verify Decision in DB
Write-Host "`n[4.1] Verifying Decision in DB..."
Execute-Sql "SELECT id, title, status, voting_type, community_id FROM decisions WHERE id = $decisionId"

# 5. CREATE 3 OPTIONS
Write-Host "`n[5] Creating 3 Options..."
$optionIds = @()
for ($i = 1; $i -le 3; $i++) {
    $optionBody = @{
        title = "Option Monitor $i"
        description = "Description for Monitor $i"
        criteria = @()
    } | ConvertTo-Json

    try {
        $optionResponse = Invoke-WebRequest -Uri "http://localhost:8080/api/decisions/$decisionId/options" -Method Post -Body $optionBody -ContentType "application/json" -Headers $headers -UseBasicParsing
        Write-Host "Option $i HTTP Status: $($optionResponse.StatusCode)"
        Write-Host "Option $i Response: $($optionResponse.Content)"
        $optJson = $optionResponse.Content | ConvertFrom-Json
        $optionIds += $optJson.id
    } catch {
        Write-Host "Failed creating option $i - $($_.Exception.Message)"
        exit 1
    }
}

# Verify Options in DB
Write-Host "`n[5.1] Verifying Options in DB..."
Execute-Sql "SELECT id, option_name, description FROM decision_options WHERE decision_id = $decisionId"

# 6. CREATE 3 COMPARISON FACTORS
Write-Host "`n[6] Creating 3 Comparison Factors..."
$factorIds = @()
$factorNames = @("Cost", "Resolution", "Ergonomics")
for ($i = 0; $i -lt 3; $i++) {
    $factorBody = @{
        name = $factorNames[$i]
        description = "Evaluation for factor $($factorNames[$i])"
    } | ConvertTo-Json

    try {
        $factorResponse = Invoke-WebRequest -Uri "http://localhost:8080/api/decisions/$decisionId/factors" -Method Post -Body $factorBody -ContentType "application/json" -Headers $headers -UseBasicParsing
        Write-Host "Factor $($i+1) HTTP Status: $($factorResponse.StatusCode)"
        Write-Host "Factor $($i+1) Response: $($factorResponse.Content)"
        $facJson = $factorResponse.Content | ConvertFrom-Json
        $factorIds += $facJson.id
    } catch {
        Write-Host "Failed creating factor $($i+1) - $($_.Exception.Message)"
        exit 1
    }
}

# Verify Factors in DB
Write-Host "`n[6.1] Verifying Factors in DB..."
Execute-Sql "SELECT id, name, description FROM comparison_factors WHERE decision_id = $decisionId"

# 7. PUBLISH DECISION
Write-Host "`n[7] Publishing Decision..."
try {
    $publishResponse = Invoke-WebRequest -Uri "http://localhost:8080/api/decisions/$decisionId/publish" -Method Post -ContentType "application/json" -Headers $headers -UseBasicParsing
    Write-Host "HTTP Status: $($publishResponse.StatusCode)"
    Write-Host "Response Content: $($publishResponse.Content)"
} catch {
    Write-Host "Failed publishing decision - $($_.Exception.Message)"
    exit 1
}

# Verify ACTIVE status in DB
Write-Host "`n[7.1] Verifying Active status in DB..."
Execute-Sql "SELECT id, title, status FROM decisions WHERE id = $decisionId"

# 8. SUBMIT COMPARISON SCORES
Write-Host "`n[8] Submitting Comparison Scores..."
# Seed score for every option-factor combination (3 options * 3 factors = 9 scores)
for ($o = 0; $o -lt 3; $o++) {
    for ($f = 0; $f -lt 3; $f++) {
        $scoreBody = @{
            optionId = $optionIds[$o]
            factorId = $factorIds[$f]
            score = 75 + $o * 5 + $f * 2  # dynamic score
            remarks = "Remarks for Option $($o+1) - Factor $($f+1)"
        } | ConvertTo-Json

        try {
            $scoreResponse = Invoke-WebRequest -Uri "http://localhost:8080/api/decisions/$decisionId/scores" -Method Post -Body $scoreBody -ContentType "application/json" -Headers $headers -UseBasicParsing
            Write-Host "Score ($($optionIds[$o]), $($factorIds[$f])) HTTP Status: $($scoreResponse.StatusCode)"
        } catch {
            Write-Host "Failed submitting score ($($optionIds[$o]), $($factorIds[$f])) - $($_.Exception.Message)"
            exit 1
        }
    }
}

# Verify Scores in DB
Write-Host "`n[8.1] Verifying Scores in DB..."
Execute-Sql "SELECT option_id, factor_id, score, remarks FROM comparison_scores WHERE option_id IN ($($optionIds -join ','))"

# 9. RETRIEVE RANKING
Write-Host "`n[9] Retrieving Ranking..."
try {
    $rankingResponse = Invoke-WebRequest -Uri "http://localhost:8080/api/decisions/$decisionId/ranking" -Method Get -Headers $headers -UseBasicParsing
    Write-Host "HTTP Status: $($rankingResponse.StatusCode)"
    Write-Host "Response Content: $($rankingResponse.Content)"
} catch {
    Write-Host "Failed retrieving ranking - $($_.Exception.Message)"
    exit 1
}

Write-Host "`n==============================================="
Write-Host "VERIFICATION FLOW COMPLETED SUCCESSFUL"
Write-Host "==============================================="
