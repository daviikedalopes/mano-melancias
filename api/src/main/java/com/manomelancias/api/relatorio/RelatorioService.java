package com.manomelancias.api.relatorio;

import com.manomelancias.api.relatorio.dto.ClienteRelatorioDTO;
import com.manomelancias.api.relatorio.dto.ContaReceberDTO;
import com.manomelancias.api.relatorio.dto.ProdutorRelatorioDTO;
import com.manomelancias.api.relatorio.dto.VendasPeriodoResponseDTO;
import com.manomelancias.api.venda.VendaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RelatorioService {

    private final VendaRepository vendaRepository;

    public VendasPeriodoResponseDTO totalVendidoPorPeriodo(LocalDate inicio, LocalDate fim) {
        VendasPeriodoResponseDTO resultado = vendaRepository.totalVendidoPorPeriodo(inicio, fim);
        resultado.setPeriodoInicio(inicio);
        resultado.setPeriodoFim(fim);
        return resultado;
    }

    public List<ProdutorRelatorioDTO> totalPorProdutor(UUID produtorId) {
        return vendaRepository.totalPorProdutor(produtorId);
    }

    public List<ClienteRelatorioDTO> totalPorCliente(UUID clienteId) {
        return vendaRepository.totalPorCliente(clienteId);
    }

    public List<ContaReceberDTO> contasAReceber() {
        return vendaRepository.contasAReceber();
    }
}
