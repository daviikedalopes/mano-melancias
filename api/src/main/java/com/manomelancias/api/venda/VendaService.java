package com.manomelancias.api.venda;

import com.manomelancias.api.cliente.Cliente;
import com.manomelancias.api.cliente.ClienteService;
import com.manomelancias.api.cliente.dto.ClienteRequestDTO;
import com.manomelancias.api.motorista.Motorista;
import com.manomelancias.api.motorista.MotoristaService;
import com.manomelancias.api.motorista.dto.MotoristaRequestDTO;
import com.manomelancias.api.produtor.Produtor;
import com.manomelancias.api.produtor.ProdutorService;
import com.manomelancias.api.produtor.dto.ProdutorRequestDTO;
import com.manomelancias.api.shared.exception.BusinessException;
import com.manomelancias.api.shared.exception.ResourceNotFoundException;
import com.manomelancias.api.usuario.Usuario;
import com.manomelancias.api.usuario.UsuarioRepository;
import com.manomelancias.api.veiculo.Veiculo;
import com.manomelancias.api.veiculo.VeiculoService;
import com.manomelancias.api.veiculo.dto.VeiculoRequestDTO;
import com.manomelancias.api.venda.dto.VendaFiltroDTO;
import com.manomelancias.api.venda.dto.VendaRequestDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VendaService {

    private static final int SCALE_PESO = 2;
    private static final int SCALE_MONEY = 2;

    private final VendaRepository vendaRepository;
    private final ClienteService clienteService;
    private final ProdutorService produtorService;
    private final MotoristaService motoristaService;
    private final VeiculoService veiculoService;
    private final UsuarioRepository usuarioRepository;

    // --- Regras de cálculo (seção 3 do documento de arquitetura) ---

    public BigDecimal calcularPesoLiquido(BigDecimal pesoBruto, BigDecimal descTara, BigDecimal descPalha) {
        return pesoBruto.subtract(descTara).subtract(descPalha).setScale(SCALE_PESO, RoundingMode.HALF_UP);
    }

    public BigDecimal calcularValorMercadoria(BigDecimal pesoLiquido, BigDecimal precoKg) {
        return pesoLiquido.multiply(precoKg).setScale(SCALE_MONEY, RoundingMode.HALF_UP);
    }

    public BigDecimal calcularValorFrete(TipoFrete tipoFrete, BigDecimal pesoLiquido, BigDecimal precoFreteKg, BigDecimal valorFreteNegociado) {
        if (tipoFrete == TipoFrete.POR_KG) {
            if (precoFreteKg == null) {
                throw new BusinessException("precoFreteKg é obrigatório quando tipoFrete = POR_KG");
            }
            return pesoLiquido.multiply(precoFreteKg).setScale(SCALE_MONEY, RoundingMode.HALF_UP);
        }

        if (valorFreteNegociado == null) {
            throw new BusinessException("valorFrete é obrigatório quando tipoFrete = NEGOCIADO");
        }
        return valorFreteNegociado.setScale(SCALE_MONEY, RoundingMode.HALF_UP);
    }

    public BigDecimal calcularRestante(BigDecimal valorMercadoria, BigDecimal valorFrete) {
        return valorMercadoria.subtract(valorFrete).setScale(SCALE_MONEY, RoundingMode.HALF_UP);
    }

    public BigDecimal calcularMediaPeso(BigDecimal pesoLiquido, Integer totalFrutas) {
        return pesoLiquido.divide(BigDecimal.valueOf(totalFrutas), SCALE_PESO, RoundingMode.HALF_UP);
    }

    public Integer gerarNumeroSequencial() {
        return vendaRepository.proximoNumero().intValue();
    }

    // --- CRUD ---

    public Venda buscarPorId(UUID id) {
        return vendaRepository.buscarPorIdComRelacionamentos(id)
                .orElseThrow(() -> new ResourceNotFoundException("Venda não encontrada: " + id));
    }

    public List<Venda> buscar(VendaFiltroDTO filtro) {
        return vendaRepository.buscar(
                filtro.getDataInicio(),
                filtro.getDataFim(),
                filtro.getClienteId(),
                filtro.getProdutorId(),
                filtro.getMotoristaId(),
                filtro.getStatusPagamento());
    }

    @Transactional
    public Venda criar(VendaRequestDTO dto) {
        Venda venda = new Venda();
        aplicarDados(venda, dto);
        venda.setNumero(gerarNumeroSequencial());
        venda.setCreatedBy(usuarioAutenticado());
        return vendaRepository.save(venda);
    }

    @Transactional
    public Venda atualizar(UUID id, VendaRequestDTO dto) {
        Venda venda = buscarPorId(id);
        aplicarDados(venda, dto);
        return vendaRepository.save(venda);
    }

    @Transactional
    public void excluir(UUID id) {
        if (!vendaRepository.existsById(id)) {
            throw new ResourceNotFoundException("Venda não encontrada: " + id);
        }
        vendaRepository.deleteById(id);
    }

    private void aplicarDados(Venda venda, VendaRequestDTO dto) {
        venda.setDataVenda(dto.getDataVenda());
        venda.setCliente(resolverCliente(dto));
        venda.setProdutor(resolverProdutor(dto));
        venda.setMotorista(resolverMotorista(dto));
        venda.setVeiculo(resolverVeiculo(dto));

        BigDecimal descPalha = dto.getDescPalha() != null ? dto.getDescPalha() : BigDecimal.ZERO;
        BigDecimal pesoLiquido = calcularPesoLiquido(dto.getPesoBruto(), dto.getDescTara(), descPalha);
        BigDecimal valorMercadoria = calcularValorMercadoria(pesoLiquido, dto.getPrecoKg());
        BigDecimal valorFrete = calcularValorFrete(dto.getTipoFrete(), pesoLiquido, dto.getPrecoFreteKg(), dto.getValorFrete());
        BigDecimal restante = calcularRestante(valorMercadoria, valorFrete);
        BigDecimal mediaPeso = calcularMediaPeso(pesoLiquido, dto.getTotalFrutas());

        venda.setPesoBruto(dto.getPesoBruto());
        venda.setDescTara(dto.getDescTara());
        venda.setDescPalha(descPalha);
        venda.setPesoLiquido(pesoLiquido);
        venda.setTotalFrutas(dto.getTotalFrutas());
        venda.setMediaPeso(mediaPeso);
        venda.setPrecoKg(dto.getPrecoKg());
        venda.setValorMercadoria(valorMercadoria);
        venda.setTipoFrete(dto.getTipoFrete());
        venda.setPrecoFreteKg(dto.getTipoFrete() == TipoFrete.POR_KG ? dto.getPrecoFreteKg() : null);
        venda.setValorFrete(valorFrete);
        venda.setRestantePagar(restante);
        venda.setVencimento(dto.getVencimento());
        venda.setNf(dto.getNf());
        venda.setStatusPagamento(dto.getStatusPagamento() != null ? dto.getStatusPagamento() : StatusPagamento.PENDENTE);
        venda.setObservacoes(dto.getObservacoes());
    }

    private Cliente resolverCliente(VendaRequestDTO dto) {
        if (dto.getClienteId() != null) {
            return clienteService.buscarPorId(dto.getClienteId());
        }
        ClienteRequestDTO novo = dto.getClienteNovo();
        if (novo != null) {
            return clienteService.buscarOuCriar(novo.getNome(), novo.getMunicipio(), novo.getEstado());
        }
        throw new BusinessException("cliente é obrigatório: informe clienteId ou clienteNovo");
    }

    private Produtor resolverProdutor(VendaRequestDTO dto) {
        if (dto.getProdutorId() != null) {
            return produtorService.buscarPorId(dto.getProdutorId());
        }
        ProdutorRequestDTO novo = dto.getProdutorNovo();
        if (novo != null) {
            return produtorService.buscarOuCriar(novo.getNome(), novo.getCidade());
        }
        throw new BusinessException("produtor é obrigatório: informe produtorId ou produtorNovo");
    }

    private Motorista resolverMotorista(VendaRequestDTO dto) {
        if (dto.getMotoristaId() != null) {
            return motoristaService.buscarPorId(dto.getMotoristaId());
        }
        MotoristaRequestDTO novo = dto.getMotoristaNovo();
        if (novo != null) {
            return motoristaService.buscarOuCriar(novo.getNome(), novo.getCpf(), novo.getTelefone());
        }
        throw new BusinessException("motorista é obrigatório: informe motoristaId ou motoristaNovo");
    }

    private Veiculo resolverVeiculo(VendaRequestDTO dto) {
        if (dto.getVeiculoId() != null) {
            return veiculoService.buscarPorId(dto.getVeiculoId());
        }
        VeiculoRequestDTO novo = dto.getVeiculoNovo();
        if (novo != null) {
            return veiculoService.buscarOuCriar(novo.getPlaca(), novo.getCidade(), novo.getMotoristaId());
        }
        throw new BusinessException("veículo é obrigatório: informe veiculoId ou veiculoNovo");
    }

    private Usuario usuarioAutenticado() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UUID usuarioId) {
            return usuarioRepository.findById(usuarioId).orElse(null);
        }
        return null;
    }
}