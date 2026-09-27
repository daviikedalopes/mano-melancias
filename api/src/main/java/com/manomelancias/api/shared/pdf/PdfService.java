package com.manomelancias.api.shared.pdf;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.manomelancias.api.relatorio.dto.VendasPeriodoResponseDTO;
import com.manomelancias.api.shared.exception.BusinessException;
import com.manomelancias.api.venda.StatusPagamento;
import com.manomelancias.api.venda.Venda;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 * Gera o recibo de venda em PDF, reproduzindo o layout da ficha "Venda" em
 * papel (data, número, destinatário, pesos, financeiro e logística).
 */
@Service
public class PdfService {

    private static final DateTimeFormatter DATA_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATA_HORA_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    // Fixo em Brasília: em produção a JVM costuma rodar em UTC e o "emitido em" sairia com horas erradas.
    private static final ZoneId FUSO = ZoneId.of("America/Sao_Paulo");
    private static final Locale PT_BR = Locale.of("pt", "BR");
    private static final Font TITULO = new Font(Font.HELVETICA, 16, Font.BOLD);
    private static final Font LABEL = new Font(Font.HELVETICA, 10, Font.BOLD);
    private static final Font VALOR = new Font(Font.HELVETICA, 10, Font.NORMAL);
    private static final Font TH = new Font(Font.HELVETICA, 9, Font.BOLD);
    private static final Font TD = new Font(Font.HELVETICA, 9, Font.NORMAL);

    public byte[] gerarReciboVenda(Venda venda) {
        try {
            Document document = new Document(PageSize.A4, 36, 36, 36, 36);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            PdfWriter.getInstance(document, out);
            document.open();

            Paragraph titulo = new Paragraph("Venda  -  Nº " + formatarNumero(venda.getNumero()), TITULO);
            titulo.setAlignment(Element.ALIGN_CENTER);
            titulo.setSpacingAfter(16);
            document.add(titulo);

            document.add(linha("Data", venda.getDataVenda().format(DATA_FMT)));
            document.add(linha("Destinatário", venda.getCliente().getNome()));
            document.add(linha("Município / Estado", venda.getCliente().getMunicipio() + " / " + venda.getCliente().getEstado()));
            document.add(espaco());

            document.add(tabelaPesos(venda));
            document.add(espaco());

            document.add(tabelaFinanceiro(venda));
            document.add(espaco());

            document.add(linha("Motorista", venda.getMotorista().getNome()));
            document.add(linha("CPF", venda.getMotorista().getCpf()));
            document.add(linha("Fone", nvl(venda.getMotorista().getTelefone())));
            document.add(linha("Placa", venda.getVeiculo().getPlaca()));
            document.add(linha("Produtor", venda.getProdutor().getNome()));
            document.add(linha("Cidade (produtor)", venda.getProdutor().getCidade()));

            if (venda.getNf() != null && !venda.getNf().isBlank()) {
                document.add(linha("NF", venda.getNf()));
            }
            if (venda.getVencimento() != null) {
                document.add(linha("Vencimento", venda.getVencimento().format(DATA_FMT)));
            }
            if (venda.getObservacoes() != null && !venda.getObservacoes().isBlank()) {
                document.add(espaco());
                document.add(linha("Observações", venda.getObservacoes()));
            }

            document.close();
            return out.toByteArray();
        } catch (Exception ex) {
            throw new BusinessException("Falha ao gerar PDF do recibo: " + ex.getMessage());
        }
    }

    /**
     * Relatório "Vendas por período": cabeçalho com os filtros aplicados, os
     * totais e a lista das vendas que compõem esses totais. A4 paisagem, porque
     * a tabela de vendas tem muitas colunas.
     */
    public byte[] gerarRelatorioVendasPeriodo(
            LocalDate inicio,
            LocalDate fim,
            String produtorNome,
            String clienteNome,
            VendasPeriodoResponseDTO totais,
            List<Venda> vendas) {
        try {
            Document document = new Document(PageSize.A4.rotate(), 36, 36, 36, 36);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            PdfWriter.getInstance(document, out);
            document.open();

            Paragraph titulo = new Paragraph("Relatório de vendas por período", TITULO);
            titulo.setAlignment(Element.ALIGN_CENTER);
            titulo.setSpacingAfter(12);
            document.add(titulo);

            document.add(linha("Período", inicio.format(DATA_FMT) + " a " + fim.format(DATA_FMT)));
            document.add(linha("Produtor", produtorNome != null ? produtorNome : "Todos"));
            document.add(linha("Cliente", clienteNome != null ? clienteNome : "Todos"));
            document.add(linha("Emitido em", LocalDateTime.now(FUSO).format(DATA_HORA_FMT)));
            document.add(espaco());

            NumberFormat moeda = NumberFormat.getCurrencyInstance(PT_BR);
            NumberFormat peso = NumberFormat.getNumberInstance(PT_BR);
            peso.setMinimumFractionDigits(2);
            peso.setMaximumFractionDigits(2);

            document.add(tabelaTotais(totais, moeda));
            document.add(espaco());

            if (vendas.isEmpty()) {
                document.add(new Paragraph("Nenhuma venda encontrada para os filtros selecionados.", VALOR));
            } else {
                document.add(tabelaVendas(vendas, moeda, peso));
            }

            document.close();
            return out.toByteArray();
        } catch (Exception ex) {
            throw new BusinessException("Falha ao gerar PDF do relatório: " + ex.getMessage());
        }
    }

    private PdfPTable tabelaTotais(VendasPeriodoResponseDTO totais, NumberFormat moeda) {
        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.addCell(celula("Vendas no período", String.valueOf(totais.getQuantidadeVendas())));
        table.addCell(celula("Mercadoria vendida", moeda.format(totais.getTotalValorMercadoria())));
        table.addCell(celula("Frete total", moeda.format(totais.getTotalValorFrete())));
        table.addCell(celula("Restante a pagar", moeda.format(totais.getTotalRestantePagar())));
        return table;
    }

    private PdfPTable tabelaVendas(List<Venda> vendas, NumberFormat moeda, NumberFormat peso) throws Exception {
        PdfPTable table = new PdfPTable(9);
        table.setWidthPercentage(100);
        table.setWidths(new float[] {6, 10, 22, 22, 11, 13, 11, 13, 11});
        table.setHeaderRows(1);

        String[] cabecalho = {"Nº", "Data", "Cliente", "Produtor", "Peso líq.", "Mercadoria", "Frete", "Restante", "Status"};
        for (int i = 0; i < cabecalho.length; i++) {
            boolean numerica = i >= 4 && i <= 7;
            PdfPCell cell = celulaTabela(cabecalho[i], TH, numerica);
            cell.setBackgroundColor(Color.LIGHT_GRAY);
            table.addCell(cell);
        }

        for (Venda v : vendas) {
            table.addCell(celulaTabela(String.valueOf(v.getNumero()), TD, false));
            table.addCell(celulaTabela(v.getDataVenda().format(DATA_FMT), TD, false));
            table.addCell(celulaTabela(v.getCliente().getNome(), TD, false));
            table.addCell(celulaTabela(v.getProdutor().getNome(), TD, false));
            table.addCell(celulaTabela(peso.format(v.getPesoLiquido()) + " kg", TD, true));
            table.addCell(celulaTabela(moeda.format(v.getValorMercadoria()), TD, true));
            table.addCell(celulaTabela(moeda.format(v.getValorFrete()), TD, true));
            table.addCell(celulaTabela(moeda.format(v.getRestantePagar()), TD, true));
            table.addCell(celulaTabela(rotuloStatus(v.getStatusPagamento()), TD, false));
        }
        return table;
    }

    private PdfPCell celulaTabela(String texto, Font fonte, boolean alinharDireita) {
        PdfPCell cell = new PdfPCell(new Paragraph(texto, fonte));
        cell.setPadding(4);
        if (alinharDireita) {
            cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        }
        return cell;
    }

    private String rotuloStatus(StatusPagamento status) {
        return switch (status) {
            case PENDENTE -> "Pendente";
            case PAGO_PARCIAL -> "Pago parcial";
            case PAGO -> "Pago";
        };
    }

    private PdfPTable tabelaPesos(Venda venda) throws Exception {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.addCell(celula("Peso bruto", kg(venda.getPesoBruto())));
        table.addCell(celula("Total de frutas", String.valueOf(venda.getTotalFrutas())));
        table.addCell(celula("Desc. tara", kg(venda.getDescTara())));
        table.addCell(celula("Média por fruta", kg(venda.getMediaPeso())));
        table.addCell(celula("Desc. palha", kg(venda.getDescPalha())));
        table.addCell(celula("Peso líquido", kg(venda.getPesoLiquido())));
        return table;
    }

    private PdfPTable tabelaFinanceiro(Venda venda) throws Exception {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.addCell(celula("Preço/kg", reais(venda.getPrecoKg())));
        table.addCell(celula("Valor mercadoria", reais(venda.getValorMercadoria())));
        table.addCell(celula("Tipo de frete", venda.getTipoFrete().name()));
        table.addCell(celula("Valor frete", reais(venda.getValorFrete()).concat(" (-)")));
        table.addCell(celula("", ""));
        table.addCell(celula("Restante a pagar", reais(venda.getRestantePagar())));
        return table;
    }

    private PdfPCell celula(String label, String valor) {
        Paragraph p = new Paragraph();
        if (!label.isBlank()) {
            p.add(new com.lowagie.text.Chunk(label + ": ", LABEL));
        }
        p.add(new com.lowagie.text.Chunk(valor, VALOR));
        PdfPCell cell = new PdfPCell(p);
        cell.setPadding(6);
        return cell;
    }

    private Paragraph linha(String label, String valor) {
        Paragraph p = new Paragraph();
        p.add(new com.lowagie.text.Chunk(label + ": ", LABEL));
        p.add(new com.lowagie.text.Chunk(valor != null ? valor : "-", VALOR));
        return p;
    }

    private Paragraph espaco() {
        return new Paragraph(" ");
    }

    private String kg(BigDecimal valor) {
        return valor.toPlainString() + " kg";
    }

    private String reais(BigDecimal valor) {
        return "R$ " + valor.toPlainString();
    }

    private String formatarNumero(Integer numero) {
        return String.format("%06d", numero);
    }

    private String nvl(String valor) {
        return valor != null && !valor.isBlank() ? valor : "-";
    }
}
