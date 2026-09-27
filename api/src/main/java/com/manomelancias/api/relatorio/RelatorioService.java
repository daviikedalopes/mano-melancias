package com.manomelancias.api.relatorio;

import com.manomelancias.api.cliente.ClienteService;
import com.manomelancias.api.produtor.ProdutorService;
import com.manomelancias.api.relatorio.dto.ClienteRelatorioDTO;
import com.manomelancias.api.relatorio.dto.ContaReceberDTO;
import com.manomelancias.api.relatorio.dto.ProdutorRelatorioDTO;
import com.manomelancias.api.relatorio.dto.VendasPeriodoResponseDTO;
import com.manomelancias.api.shared.pdf.PdfService;
import com.manomelancias.api.venda.Venda;
import com.manomelancias.api.venda.VendaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RelatorioService {

    private final VendaRepository vendaRepository;
    private final ProdutorService produtorService;
    private final ClienteService clienteService;
    private final PdfService pdfService;

    public VendasPeriodoResponseDTO totalVendidoPorPeriodo(
            LocalDate inicio, LocalDate fim, UUID produtorId, UUID clienteId) {
        VendasPeriodoResponseDTO resultado =
                vendaRepository.totalVendidoPorPeriodo(inicio, fim, produtorId, clienteId);
        resultado.setPeriodoInicio(inicio);
        resultado.setPeriodoFim(fim);
        return resultado;
    }

    public byte[] gerarPdfVendasPeriodo(LocalDate inicio, LocalDate fim, UUID produtorId, UUID clienteId) {
        String produtorNome = produtorId != null ? produtorService.buscarPorId(produtorId).getNome() : null;
        String clienteNome = clienteId != null ? clienteService.buscarPorId(clienteId).getNome() : null;

        VendasPeriodoResponseDTO totais = totalVendidoPorPeriodo(inicio, fim, produtorId, clienteId);

        // A busca devolve da mais nova para a mais antiga; o relatório lê melhor em ordem cronológica.
        List<Venda> vendas = new ArrayList<>(vendaRepository.buscar(inicio, fim, clienteId, produtorId, null, null));
        Collections.reverse(vendas);

        return pdfService.gerarRelatorioVendasPeriodo(inicio, fim, produtorNome, clienteNome, totais, vendas);
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
