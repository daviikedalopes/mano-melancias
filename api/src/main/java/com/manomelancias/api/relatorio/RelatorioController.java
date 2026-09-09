package com.manomelancias.api.relatorio;

import com.manomelancias.api.relatorio.dto.ClienteRelatorioDTO;
import com.manomelancias.api.relatorio.dto.ContaReceberDTO;
import com.manomelancias.api.relatorio.dto.ProdutorRelatorioDTO;
import com.manomelancias.api.relatorio.dto.VendasPeriodoResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
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
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        return relatorioService.totalVendidoPorPeriodo(inicio, fim);
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
