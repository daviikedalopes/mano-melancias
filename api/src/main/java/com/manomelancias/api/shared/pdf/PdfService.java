package com.manomelancias.api.shared.pdf;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.manomelancias.api.shared.exception.BusinessException;
import com.manomelancias.api.venda.Venda;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

/**
 * Gera o recibo de venda em PDF, reproduzindo o layout da ficha "Venda" em
 * papel (data, número, destinatário, pesos, financeiro e logística).
 */
@Service
public class PdfService {

    private static final DateTimeFormatter DATA_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final Font TITULO = new Font(Font.HELVETICA, 16, Font.BOLD);
    private static final Font LABEL = new Font(Font.HELVETICA, 10, Font.BOLD);
    private static final Font VALOR = new Font(Font.HELVETICA, 10, Font.NORMAL);

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
