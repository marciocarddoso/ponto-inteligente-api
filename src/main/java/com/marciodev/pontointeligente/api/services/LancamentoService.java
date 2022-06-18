package com.marciodev.pontointeligente.api.services;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import com.marciodev.pontointeligente.api.entidades.Lancamento;

public interface LancamentoService {
	
	/**
	 * Retorna uma lista paginada de lançamentos de um determinado funcionário.
	 * 
	 * @param funcionarioId
	 * @param pageRequest
	 * @return Page<Lancamento>
	 */
	Page<Lancamento> buscarPorFuncionarioId(Long funcionarioId, PageRequest pageRequest);
	
	/**
	 * Retorna um lançamento por ID.
	 * 
	 * @param id
	 * @return Optional<Lancamento>
	 */
	Optional<Lancamento> buscarPorId(Long id);
	
	/**
	 * Persiste um lançamento na base de dados.
	 * 
	 * @param lancamento
	 * @return Lancamento
	 */
	Lancamento persistir(Lancamento lancamento);
	
	/**
	 * Remove um lançamento da base de dados.
	 * 
	 * @param id
	 */
	void remover(Long id);

	/**
	 * Retorna o último lançamento por ID de funcionário.
	 *
	 * @param funcionarioId
	 * @return Optional<Lancamento>
	 */
    Optional<Lancamento> buscarUltimoPorFuncionarioId(Long funcionarioId);

	/**
	 * Retorna uma lista com todos os lançamentos de um determinado funcionário.
	 *
	 * @param funcionarioId
	 * @return List<Lancamento>
	 */
    List<Lancamento> buscarTodosPorFuncionarioId(Long funcionarioId);

}
