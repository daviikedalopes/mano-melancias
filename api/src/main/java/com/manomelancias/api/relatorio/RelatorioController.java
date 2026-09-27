package com.manomelancias.api.relatorio;

import com.manomelancias.api.relatorio.dto.ClienteRelatorioDTO;
import com.manomelancias.api.relatorio.dto.ContaReceberDTO;
import com.manomelancias.api.relatorio.dto.ProdutorRelatorioDTO;
import com.manomelancias.api.relatorio.dto.VendasPeriodoResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/relatorios")
@RequiredArgsConstructor
public class RelatorioController {

    private final RelatorioService relatorioService;

    @GetMapping("/vendas-periodo")
    public VendasPeriodoResponseDTO vendasPeriodo(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim,
            @RequestParam(required = false) UUID produtorId,
            @RequestParam(required = false) UUID clienteId) {
        return relatorioService.totalVendidoPorPeriodo(inicio, fim, produtorId, clienteId);
    }

    @GetMapping("/vendas-periodo/pdf")
    public ResponseEntity<byte[]> vendasPeriodoPdf(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim,
            @RequestParam(required = false) UUID produtorId,
            @RequestParam(required = false) UUID clienteId) {
        byte[] pdf = relatorioService.gerarPdfVendasPeriodo(inicio, fim, produtorId, clienteId);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=relatorio-vendas-" + inicio + "-a-" + fim + ".pdf")
                .body(pdf);
    }

    @GetMapping("/por-produtor")
    public List<ProdutorRelatorioDTO> porProdutor(@RequestParam(required = false) UUID produtorId) {
        return relatorioService.totalPorProdutor(produtorId);
    }

    @GetMapping("/por-cliente")
    public List<ClienteRelatorioDTO> porCliente(@RequestParam(required = false) UUID clienteId) {
        return relatorioService.totalPorCliente(clienteId);
    }

    @GetMapping("/contas-a-receber")
    public List<ContaReceberDTO> contasAReceber() {
        return relatorioService.contasAReceber();
    }
}
