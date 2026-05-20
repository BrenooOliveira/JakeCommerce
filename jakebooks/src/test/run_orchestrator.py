#!/usr/bin/env python3
"""Orquestrador para rodar os testes Playwright em sequência com logs e validações.

Uso:
    python run_orchestrator.py

Executa:
  1. Teste de compra do cliente
  2. Teste de despacho de pedidos (admin)
  3. Teste de solicitação de troca (cliente)
  4. Teste de autorização de troca (admin)
  5. Teste de uso do cupom de troca (cliente)
"""
from pathlib import Path
import subprocess
import sys
import time
import logging
from datetime import datetime

logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s ORCHESTRATOR %(levelname)s %(message)s'
)
logger = logging.getLogger(__name__)

ROOT = Path(__file__).resolve().parent
SHARED_FILE = ROOT / "shared_troca_id.txt"

tests = [
    ROOT / "1.test_compra_cliente.py",
    ROOT / "2.test_despacho_pedidos_admin.py",
    ROOT / "3.test_solicitar_troca_cliente.py",
    ROOT / "4.test_autorizar_troca_e_despacho_admin.py",
    ROOT / "5.test_usando_cupom_troca_cliente.py"
]


def run_script(path: Path, test_number: int, total_tests: int) -> int:
    logger.info(f"[{test_number}/{total_tests}] Iniciando: {path.name}")
    logger.info(f"Objetivo: {get_test_description(test_number)}")
    
    cmd = [sys.executable, str(path)]
    start_time = time.time()
    
    try:
        proc = subprocess.run(cmd)
        elapsed = time.time() - start_time
        
        if proc.returncode == 0:
            logger.info(f"✓ {path.name} PASSOU (tempo: {elapsed:.1f}s)")
            return proc.returncode
        else:
            logger.error(f"✗ {path.name} FALHOU com exit code {proc.returncode} (tempo: {elapsed:.1f}s)")
            return proc.returncode
    except Exception as e:
        logger.error(f"✗ Erro ao executar {path.name}: {e}")
        return 1


def get_test_description(test_num: int) -> str:
    descriptions = {
        1: "Compra de livros pelo cliente",
        2: "Despacho de pedidos pelo admin",
        3: "Solicitação de troca pelo cliente",
        4: "Autorização e recebimento de troca pelo admin",
        5: "Uso do cupom de troca para nova compra"
    }
    return descriptions.get(test_num, "Teste desconhecido")


def validate_shared_file_exists(test_num: int) -> bool:
    """Valida se o arquivo compartilhado será necessário no próximo teste."""
    # Teste 5 precisa do arquivo gerado pelo teste 4
    if test_num == 4:
        return True
    return False


def main():
    logger.info("="*70)
    logger.info("INICIANDO SUITE DE TESTES E2E DO JAKEBOOKS")
    logger.info(f"Total de testes a executar: {len(tests)}")
    logger.info("="*70)
    
    # Validar existência de todos os arquivos antes de começar
    logger.info("Validando existência dos arquivos de teste...")
    for idx, t in enumerate(tests, 1):
        if not t.exists():
            logger.error(f"Arquivo de teste não encontrado: {t}")
            sys.exit(2)
        logger.info(f"  [{idx}] ✓ {t.name}")
    
    logger.info("-"*70)
    
    # Limpar arquivo compartilhado antes de começar
    if SHARED_FILE.exists():
        logger.info(f"Limpando arquivo compartilhado: {SHARED_FILE.name}")
        SHARED_FILE.unlink()
    
    # Executar testes
    for idx, test_path in enumerate(tests, 1):
        try:
            rc = run_script(test_path, idx, len(tests))
            
            if rc != 0:
                logger.error("="*70)
                logger.error(f"SUITE INTERROMPIDA: {test_path.name} falhou")
                logger.error("="*70)
                sys.exit(rc)
            
            # Validar arquivo compartilhado após teste 4
            if idx == 4:
                logger.info("Validando arquivo compartilhado gerado pelo teste 4...")
                time.sleep(1)  # Aguardar escrita em disco
                if SHARED_FILE.exists():
                    cupom = SHARED_FILE.read_text(encoding="utf-8").strip()
                    logger.info(f"✓ Cupom de troca capturado com sucesso: {cupom}")
                else:
                    logger.error(f"✗ Arquivo compartilhado não foi gerado: {SHARED_FILE}")
                    sys.exit(3)
            
            # Delay entre testes (exceto o último)
            if idx < len(tests):
                delay = 3
                logger.info(f"Aguardando {delay}s antes do próximo teste...")
                time.sleep(delay)
                logger.info("-"*70)
        
        except KeyboardInterrupt:
            logger.warning("Testes interrompidos pelo usuário (Ctrl+C)")
            sys.exit(130)
    
    logger.info("="*70)
    logger.info("✓ TODOS OS TESTES PASSARAM COM SUCESSO!")
    logger.info("="*70)


if __name__ == "__main__":
    main()
