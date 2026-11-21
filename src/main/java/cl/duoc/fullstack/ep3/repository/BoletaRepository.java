package cl.duoc.fullstack.ep3.repository;

import cl.duoc.fullstack.ep3.model.Boleta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BoletaRepository extends JpaRepository<Boleta, Long> {

    List<Boleta> findAllByOrderByIdDesc();

    List<Boleta> findByClienteUsernameOrderByIdDesc(String username);

    boolean existsByNumero(String numero);
}
