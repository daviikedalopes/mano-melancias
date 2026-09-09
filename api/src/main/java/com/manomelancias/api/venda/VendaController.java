package com.manomelancias.api.venda;

import com.manomelancias.api.shared.pdf.PdfService;
import com.manomelancias.api.venda.dto.VendaFiltroDTO;
import com.manomelancias.api.venda.dto.VendaRequestDTO;
import com.manomelancias.api.venda.dto.VendaResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/vendas")
@RequiredArgsConstructor
public class VendaController {

    private final VendaService vendaService;
    private final PdfService pdfService;

    @GetMapping
    public List<VendaResponseDTO> listar(@ModelAttribute VendaFiltroDTO filtro) {
        return vendaService.buscar(filtro).stream()
                .map(VendaResponseDTO::from)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public VendaResponseDTO buscarPorId(@PathVariable UUID id) {
        return VendaResponseDTO.from(vendaService.buscarPorId(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public VendaResponseDTO criar(@Valid @RequestBody VendaRequestDTO dto) {
        return VendaResponseDTO.from(vendaService.criar(dto));
    }

    @PutMapping("/{id}")
    public VendaResponseDTO atualizar(@PathVariable UUID id, @Valid @RequestBody VendaRequestDTO dto) {
        return VendaResponseDTO.from(vendaService.atualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable UUID id) {
        vendaService.excluir(id);
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> gerarPdf(@PathVariable UUID id) {
        Venda venda = vendaService.buscarPorId(id);
        byte[] pdf = pdfService.gerarReciboVenda(venda);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=venda-" + venda.getNumero() + ".pdf")
                .body(pdf);
    }
}
