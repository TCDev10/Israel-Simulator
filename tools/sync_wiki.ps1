<#
.SYNOPSIS
    Sincronizza i file della cartella 'wiki/' con il repository GitHub Wiki di Israel-Simulator.
.DESCRIPTION
    Clona o aggiorna il repository wiki remoto (https://github.com/TCDev10/Israel-Simulator.wiki.git),
    copia tutti i file markdown da 'wiki/', esegue il commit e il push su GitHub.
#>

[CmdletBinding()]
param(
    [string]$RepoUrl = "https://github.com/TCDev10/Israel-Simulator.wiki.git"
)

$ErrorActionPreference = "Continue"

Write-Host "=================================================" -ForegroundColor Cyan
Write-Host "   Sincronizzazione GitHub Wiki - Israel Simulator" -ForegroundColor Cyan
Write-Host "=================================================" -ForegroundColor Cyan

# 1. Recupero token di autenticazione da GitHub CLI o variabile d'ambiente
$token = $env:GITHUB_TOKEN
if (-not $token) {
    $token = $env:GH_TOKEN
}
if (-not $token) {
    try {
        $token = (gh auth token 2>$null)
    } catch {}
}

if ($token) {
    $authRepoUrl = $RepoUrl -replace "https://", "https://x-access-token:$token@"
} else {
    $authRepoUrl = $RepoUrl
    Write-Warning "Nessun token trovato via 'gh auth token'. Verrà usata l'autenticazione git standard."
}

# 2. Controllo disponibilità del repository della wiki
Write-Host "Verifica disponibilità del repository wiki remoto..." -ForegroundColor Yellow
$gitOutput = & git ls-remote $authRepoUrl 2>$null
$repoExists = ($LASTEXITCODE -eq 0)


if (-not $repoExists) {
    Write-Host ""
    Write-Host "ATTENZIONE: Il repository Wiki non è ancora stato inizializzato su GitHub!" -ForegroundColor Red
    Write-Host ""
    Write-Host "Perché accade questo?" -ForegroundColor Yellow
    Write-Host "GitHub crea fisicamente il repository Git della wiki solo dopo che il proprietario"
    Write-Host "ha creato la prima pagina dall'interfaccia web."
    Write-Host ""
    Write-Host "COSA FARE (1 minuto):" -ForegroundColor Green
    Write-Host "1. Apri nel browser: https://github.com/TCDev10/Israel-Simulator/wiki"
    Write-Host "2. Clicca sul pulsante verde 'Create the first page'"
    Write-Host "3. Clicca in basso a destra su 'Save page'"
    Write-Host "4. Rilancia questo script: .\tools\sync_wiki.ps1"
    Write-Host ""
    return
}

# 3. Cartella temporanea di lavoro per la wiki
$tempWikiDir = Join-Path $env:TEMP "israel_simulator_wiki_sync"
if (Test-Path $tempWikiDir) {
    Remove-Item -Recurse -Force $tempWikiDir
}

Write-Host "Clonazione della wiki in corso..." -ForegroundColor Yellow
git clone $authRepoUrl $tempWikiDir
if ($LASTEXITCODE -ne 0) {
    Write-Error "Impossibile clonare la wiki."
    return
}

# 4. Copia dei file della wiki
Write-Host "Copia dei file da 'wiki/' al repository wiki..." -ForegroundColor Yellow
$wikiSource = Join-Path $PSScriptRoot "..\wiki"
Get-ChildItem -Path $wikiSource -Filter *.md | ForEach-Object {
    Copy-Item $_.FullName -Destination $tempWikiDir -Force
    Write-Host "  -> Copiato $($_.Name)" -ForegroundColor Gray
}

# 5. Commit e Push
Push-Location $tempWikiDir
try {
    git config user.name "TCDev"
    git config user.email "siforse99@gmail.com"
    git add -A

    $status = git status --porcelain
    if (-not $status) {
        Write-Host "La Wiki e' gia' perfettamente aggiornata!" -ForegroundColor Green
    } else {
        git commit -m "docs(wiki): update and sync all wiki documentation pages"
        git push origin HEAD
        Write-Host "SUCCESS: Tutte le pagine della Wiki sono state pubblicate con successo su GitHub!" -ForegroundColor Green
        Write-Host "Visualizzabile a: https://github.com/TCDev10/Israel-Simulator/wiki" -ForegroundColor Cyan
    }
} finally {
    Pop-Location
    Remove-Item -Recurse -Force $tempWikiDir -ErrorAction SilentlyContinue
}
