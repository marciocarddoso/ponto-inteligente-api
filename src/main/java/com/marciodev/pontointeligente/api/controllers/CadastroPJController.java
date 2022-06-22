package com.marciodev.pontointeligente.api.controllers;

import java.security.NoSuchAlgorithmException;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.marciodev.pontointeligente.api.dtos.CadastroPJDto;
import com.marciodev.pontointeligente.api.entidades.Empresa;
import com.marciodev.pontointeligente.api.entidades.Funcionario;
import com.marciodev.pontointeligente.api.enumerados.PerfilEnum;
import com.marciodev.pontointeligente.api.response.Response;
import com.marciodev.pontointeligente.api.services.EmpresaService;
import com.marciodev.pontointeligente.api.services.FuncionarioService;
import com.marciodev.pontointeligente.api.utils.PasswordUtils;

@RestController
@RequestMapping("/api/cadastrar-pj")
@CrossOrigin(origins = "*") // dominios distitnts de requisição o ideal e permitir uma url do seu servidor criada por vc, mas depende muito da arquitetura que será utilizada
public class CadastroPJController {

	private static final Logger log = LoggerFactory.getLogger(CadastroPJController.class);

	@Autowired
	private FuncionarioService funcionarioService;

	@Autowired
	private EmpresaService empresaService;

	public CadastroPJController() {
	}

	/**
	 * Cadastra uma pessoa jurídica no sistema.
	 * 
	 * @param cadastroPJDto
	 * @param result
	 * @return ResponseEntity<Response<CadastroPJDto>>
	 * @throws NoSuchAlgorithmException
	 */
	@PostMapping // é uma requisição post para cadastro
	// vai retornar um responseEntity que é o DTO a ser utilizado para cadastro
	//o requestBody vai pegar os dados do request e conventer em um DTO o valid chamara a validação das anaotações colocadas no DTO
	// bindinResult terá a informação do resultado dessa validação do DTO que pode lançar uma execeção NoSuchAlgorithmException que é relacioanda a geração de senha
	public ResponseEntity<Response<CadastroPJDto>> cadastrar(@Valid @RequestBody CadastroPJDto cadastroPJDto,
			BindingResult result) throws NoSuchAlgorithmException {
		log.info("Cadastrando PJ: {}", cadastroPJDto.toString());
		//criando uma instancia do reesponse para retorno dos dados
		Response<CadastroPJDto> response = new Response<CadastroPJDto>();

		// validação dos dados caso existam no banco com duplicidade, validação de cnpj, cpf, lanãrá no response a msg da validação
		validarDadosExistentes(cadastroPJDto, result);
		
		//realizar conversão dos dados do DTO do response para a entidade em que será persistida no banco
		Empresa empresa = this.converterDtoParaEmpresa(cadastroPJDto);
		
		//realizar conversão dos dados do DTO do response para a entidade em que será persistida no banco
		Funcionario funcionario = this.converterDtoParaFuncionario(cadastroPJDto, result);

		//realizando validações caso hajam erros no result do binding
		if (result.hasErrors()) {
			//identifica os erros
			log.error("Erro validando dados de cadastro PJ: {}", result.getAllErrors());
			//retorna cada um dos erros populados
			result.getAllErrors().forEach(error -> response.getErrors().add(error.getDefaultMessage()));
			return ResponseEntity.badRequest().body(response);
		}

		//não ocorrendo erros o fluxo de persistencia da empresa e funcionario
		this.empresaService.persistir(empresa);
		//setando o funcionario na empresa
		funcionario.setEmpresa(empresa);
		//persistidno o funcionario
		this.funcionarioService.persistir(funcionario);

		//Com os dados persistidos é necessário fazer o conversão do DTOS e retornar os dados do response populando o dto com os dados da empresa e do funcionario
		response.setData(this.converterCadastroPJDto(funcionario));
		return ResponseEntity.ok(response);
	}

	/**
	 * Verifica se a empresa ou funcionário já existem na base de dados.
	 * 
	 * @param cadastroPJDto
	 * @param result
	 */
	private void validarDadosExistentes(CadastroPJDto cadastroPJDto, BindingResult result) {
		this.empresaService.buscarPorCnpj(cadastroPJDto.getCnpj())
				.ifPresent(emp -> result.addError(new ObjectError("empresa", "Empresa já existente.")));

		this.funcionarioService.buscarPorCpf(cadastroPJDto.getCpf())
				.ifPresent(func -> result.addError(new ObjectError("funcionario", "CPF já existente.")));

		this.funcionarioService.buscarPorEmail(cadastroPJDto.getEmail())
				.ifPresent(func -> result.addError(new ObjectError("funcionario", "Email já existente.")));
	}

	/**
	 * Converte os dados do DTO para empresa.
	 * 
	 * @param cadastroPJDto
	 * @return Empresa
	 */
	private Empresa converterDtoParaEmpresa(CadastroPJDto cadastroPJDto) {
		Empresa empresa = new Empresa();
		empresa.setCnpj(cadastroPJDto.getCnpj());
		empresa.setRazaoSocial(cadastroPJDto.getRazaoSocial());

		return empresa;
	}

	/**
	 * Converte os dados do DTO para funcionário.
	 * 
	 * @param cadastroPJDto
	 * @param result
	 * @return Funcionario
	 * @throws NoSuchAlgorithmException
	 */
	private Funcionario converterDtoParaFuncionario(CadastroPJDto cadastroPJDto, BindingResult result)
			throws NoSuchAlgorithmException {
		Funcionario funcionario = new Funcionario();
		funcionario.setNome(cadastroPJDto.getNome());
		funcionario.setEmail(cadastroPJDto.getEmail());
		funcionario.setCpf(cadastroPJDto.getCpf());
		funcionario.setPerfil(PerfilEnum.ROLE_ADMIN);
		funcionario.setSenha(PasswordUtils.gerarBCrypt(cadastroPJDto.getSenha()));

		return funcionario;
	}

	/**
	 * Popula o DTO de cadastro com os dados do funcionário e empresa.
	 * 
	 * @param funcionario
	 * @return CadastroPJDto
	 */
	private CadastroPJDto converterCadastroPJDto(Funcionario funcionario) {
		CadastroPJDto cadastroPJDto = new CadastroPJDto();
		cadastroPJDto.setId(funcionario.getId());
		cadastroPJDto.setNome(funcionario.getNome());
		cadastroPJDto.setEmail(funcionario.getEmail());
		cadastroPJDto.setCpf(funcionario.getCpf());
		cadastroPJDto.setRazaoSocial(funcionario.getEmpresa().getRazaoSocial());
		cadastroPJDto.setCnpj(funcionario.getEmpresa().getCnpj());

		return cadastroPJDto;
	}

}