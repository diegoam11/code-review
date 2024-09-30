package Sigesaca.Backend.Backend.services.Impl;

import Sigesaca.Backend.Backend.models.*;
import Sigesaca.Backend.Backend.repositories.*;
import Sigesaca.Backend.Backend.services.GrupoHorarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class GrupoHorarioServiceImpl implements GrupoHorarioService {
    @Autowired
    private GrupoHorarioRepository grupoHorarioRepository;
    @Autowired
    private GrupoRepository grupoRepository;
    @Autowired
    AulaRepository aulaRepository;
    @Autowired
    CursoRepository cursoRepository;
    @Autowired
    CursoEquivalenteRepository cursoEquivalenteRepository;

    @Override
    public GrupoHorarioEntity save(GrupoHorarioEntity grupoHorario) {
        return grupoHorarioRepository.save(grupoHorario);
    }

    @Override
    public List<GrupoHorarioEntity> all() {
        return grupoHorarioRepository.findAll();
    }

    @Override
    public Optional<GrupoHorarioEntity> findById(Integer id) {
        return grupoHorarioRepository.findById(id);
    }

    @Override
    public boolean deleteById(Integer id) {
        Optional<GrupoHorarioEntity> GrupoHorario = grupoHorarioRepository.findById(id);
        if (GrupoHorario.isEmpty()) {
            return false;
        }
        grupoHorarioRepository.delete(GrupoHorario.get());
        return true;
    }

    @Override
    public List<?> findByGruINumeroAndSemICodigoAndPlanICodigo(Integer GruINumero, Integer SemICodigo, Integer PlanId, Integer CurISemestre) {
        List<CursoEntity> CursosPlan = cursoRepository.findAllByPlaestICodigoAndCurISemestre(PlanId, CurISemestre);
        List<GrupoEntity> Grupos = new ArrayList<>();
        for (CursoEntity curso : CursosPlan) {
            List<GrupoEntity> GruposCursos = grupoRepository.findAllByGruINumeroAndSemICodigoAndCurICodigo(GruINumero, SemICodigo, curso.getCurICodigo());
            Grupos.addAll(GruposCursos);
        }
        ArrayList<GrupoHorarioEntity> GruposHorarios = new ArrayList<>();
        for (GrupoEntity grupo : Grupos) {
            List<GrupoHorarioEntity> GrupoHorarios = new ArrayList<>();
            GrupoHorarios = grupoHorarioRepository.findAllByGruICodigo(grupo.getGruICodigo());
            GruposHorarios.addAll(GrupoHorarios);
        }
        List<Map> InfoReturn = new ArrayList<>();
        for (GrupoHorarioEntity grupoHorario : GruposHorarios) {
            Map<String, Object> Data = new HashMap<>();
            Data.put("gruhorICodigo", grupoHorario.getGruhorICodigo());
            Data.put("gruICodigo", grupoHorario.getGrupoByGruICodigo().getGruICodigo());
            Data.put("gruhorTHoraInicio", grupoHorario.getGruhorTHoraInicio());
            Data.put("gruhorTHoraFinal", grupoHorario.getGruhorTHoraFinal());
            Map<String, Object> Curso = new HashMap<>();
            Curso.put("curICodigo", grupoHorario.getGrupoByGruICodigo().getCursoByCurICodigo().getCurICodigo());
            Curso.put("curVcNombre", grupoHorario.getGrupoByGruICodigo().getCursoByCurICodigo().getCurVcNombre());
            Data.put("curso", Curso);
            Map<String, Object> Dia = new HashMap<>();
            Dia.put("diaICodigo", grupoHorario.getDiaByDiaICodigo().getDiaICodigo());
            Dia.put("diaINumero", grupoHorario.getDiaByDiaICodigo().getDiaINumero());
            Data.put("dia", Dia);
            if (grupoHorario.getAulaByAulICodigo() != null) {
                Map<String, Object> Aula = new HashMap<>();
                Aula.put("aulICodigo", grupoHorario.getAulaByAulICodigo().getAulICodigo());
                Aula.put("aulVcCodigo", grupoHorario.getAulaByAulICodigo().getAulVcCodigo());
                Data.put("aula", Aula);
            }
            Map<String, Object> TipoDictado = new HashMap<>();
            TipoDictado.put("curtipICodigo", grupoHorario.getCursoTipodictadoByCurtipICodigo().getCurtipICodigo());
            TipoDictado.put("curtipVcNombre", grupoHorario.getCursoTipodictadoByCurtipICodigo().getCurtipVcNombre());
            Data.put("tipoDictado", TipoDictado);
            InfoReturn.add(Data);
        }
        return InfoReturn;
    }

    @Override
    public ResponseEntity<?> AulasDisponibles(Integer GrupoHorarioId, Integer semestreId) {
        List<AulaEntity> AulasDisponibles = new ArrayList<>();
        Optional<GrupoHorarioEntity> GrupoHorario = grupoHorarioRepository.findByGruhorICodigo(GrupoHorarioId);
        List<AulaEntity> TodasAulas = aulaRepository.findAll();
        if (GrupoHorario.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        for (AulaEntity aula : TodasAulas) {
            int x = 0;
            List<GrupoHorarioEntity> GruposMismoDia = grupoHorarioRepository.findAllByAulICodigoAndDiaICodigo(aula.getAulICodigo(), GrupoHorario.get().getDiaICodigo());
            List<GrupoHorarioEntity> horariosFilteredBySemestre = new ArrayList<>();

            for (GrupoHorarioEntity g : GruposMismoDia) {
                if (semestreId.equals(g.getGrupoByGruICodigo().getSemICodigo())) {
                    horariosFilteredBySemestre.add(g);
                }
            }

            for (GrupoHorarioEntity grupo : horariosFilteredBySemestre) {
                if (grupo.getGruhorTHoraInicio().toLocalTime().isAfter(GrupoHorario.get().getGruhorTHoraInicio().toLocalTime()) && grupo.getGruhorTHoraInicio().toLocalTime().isBefore(GrupoHorario.get().getGruhorTHoraFinal().toLocalTime())) {
                    x = 1;
                }
                if ((grupo.getGruhorTHoraFinal().toLocalTime().isAfter(GrupoHorario.get().getGruhorTHoraInicio().toLocalTime()) && grupo.getGruhorTHoraFinal().toLocalTime().isBefore(GrupoHorario.get().getGruhorTHoraFinal().toLocalTime()))) {
                    x = 1;
                }
                if ((grupo.getGruhorTHoraInicio().equals(GrupoHorario.get().getGruhorTHoraInicio())) || (grupo.getGruhorTHoraFinal().equals(GrupoHorario.get().getGruhorTHoraFinal()))) {
                    x = 1;
                }
                if ((GrupoHorario.get().getGruhorTHoraInicio().toLocalTime().isAfter(grupo.getGruhorTHoraInicio().toLocalTime()) && GrupoHorario.get().getGruhorTHoraFinal().toLocalTime().isBefore(grupo.getGruhorTHoraFinal().toLocalTime()))) {
                    x = 1;
                }
                if ((GrupoHorario.get().getGruhorTHoraInicio().toLocalTime().isBefore(grupo.getGruhorTHoraInicio().toLocalTime()) && GrupoHorario.get().getGruhorTHoraFinal().toLocalTime().isAfter(grupo.getGruhorTHoraFinal().toLocalTime()))) {
                    x = 1;
                }
            }

            if (x == 0) {
                AulasDisponibles.add(aula);
            }
        }

        return new ResponseEntity<>(AulasDisponibles, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<?> findByGruId(Integer id) {
        List<GrupoHorarioEntity> grupos = grupoHorarioRepository.findAllByGruICodigo(id);
        return new ResponseEntity<>(grupos, HttpStatus.OK);
    }

    @Override
    public List<Object> findAllByGruICodigoInfo(Integer GruICodigo) {
        List<Object> InfoReturn = new ArrayList<>();
        List<GrupoHorarioEntity> GruposHorarios = grupoHorarioRepository.findAllByGruICodigo(GruICodigo);
        for (GrupoHorarioEntity grupo : GruposHorarios) {
            Map<String, Object> Data = new HashMap<>();
            Data.put("gruhorICodigo", grupo.getGruhorICodigo());
            Data.put("gruhorTHoraInicio", grupo.getGruhorTHoraInicio());
            Data.put("gruhorTHoraFinal", grupo.getGruhorTHoraFinal());
            Data.put("docICodigo", grupo.getDocICodigo());
            Map<String, Object> Grupo = new HashMap<>();
            Grupo.put("gruICodigo", grupo.getGrupoByGruICodigo().getGruICodigo());
            Grupo.put("gruINumero", grupo.getGrupoByGruICodigo().getGruINumero());
            Grupo.put("gruICapacidad", grupo.getGrupoByGruICodigo().getGruICapacidad());
            Data.put("grupo", Grupo);
            Map<String, Object> Dia = new HashMap<>();
            Dia.put("diaICodigo", grupo.getDiaByDiaICodigo().getDiaICodigo());
            Dia.put("diaINumero", grupo.getDiaByDiaICodigo().getDiaINumero());
            Dia.put("diaVcNombre", grupo.getDiaByDiaICodigo().getDiaVcNombre());
            Data.put("dia", Dia);
            Map<String, Object> Curso = new HashMap<>();
            Curso.put("curICodigo", grupo.getGrupoByGruICodigo().getCursoByCurICodigo().getCurICodigo());
            Curso.put("curVcNombre", grupo.getGrupoByGruICodigo().getCursoByCurICodigo().getCurVcNombre());
            Data.put("curso", Curso);
            if (grupo.getAulaByAulICodigo() != null) {
                Map<String, Object> Aula = new HashMap<>();
                Aula.put("aulICodigo", grupo.getAulaByAulICodigo().getAulICodigo());
                Aula.put("aulVcCodigo", grupo.getAulaByAulICodigo().getAulVcCodigo());
                Aula.put("aulICapacidad", grupo.getAulaByAulICodigo().getAulICapacidad());
                Data.put("aula", Aula);
            }
            Map<String, Object> TipoDictado = new HashMap<>();
            TipoDictado.put("curtipICodigo", grupo.getCursoTipodictadoByCurtipICodigo().getCurtipICodigo());
            TipoDictado.put("curtipVcNombre", grupo.getCursoTipodictadoByCurtipICodigo().getCurtipVcNombre());
            Data.put("tipoDictado", TipoDictado);
            InfoReturn.add(Data);
        }
        return InfoReturn;
    }

    @Override
    public List<?> asignAula(Integer gruhorId, Integer cursoId, Integer semestreId) {
        Optional<GrupoHorarioEntity> currentGrupoHorario = grupoHorarioRepository.findByGruhorICodigo(gruhorId);
        List<CursoEquivalenteEntity> cursosEq = new ArrayList<>();
        List<GrupoEntity> gruposEq = new ArrayList<>();

        for (CursoEquivalenteEntity cursoEq : cursoEquivalenteRepository.findAll()) {
            if (cursoEq.getCurICodigo().equals(cursoId) || cursoEq.getEquICodigo().equals(cursoId)) {
                cursosEq.add(cursoEq);
            }
        }

        for (CursoEquivalenteEntity cursoEq : cursosEq) {
            if (cursoEq.getCurICodigo().equals(cursoId)) {
                gruposEq.addAll(
                        grupoRepository.findByCurICodigo(cursoEq.getEquICodigo())
                                .stream()
                                .filter(grupo -> grupo.getSemICodigo().equals(semestreId))
                                .toList()

                );
            } else {
                gruposEq.addAll(
                        grupoRepository.findByCurICodigo(cursoEq.getCurICodigo())
                                .stream()
                                .filter(grupo -> grupo.getSemICodigo().equals(semestreId))
                                .toList()

                );
            }
        }

        List<GrupoHorarioEntity> horarios = new ArrayList<>();

        List<GrupoHorarioEntity> horariosEq = new ArrayList<>();
        currentGrupoHorario.ifPresent(horariosEq::add);

        for (GrupoEntity grupo : gruposEq) {
            horarios.addAll(grupoHorarioRepository.findAllByGruICodigo(grupo.getGruICodigo()));
        }

        if (currentGrupoHorario.isPresent()) {
            GrupoHorarioEntity currentHorario = currentGrupoHorario.get();
            if (currentHorario.getDocICodigo() != null) {
                for (GrupoHorarioEntity horario : horarios) {
                    if (
                            currentHorario.getDiaICodigo().equals(horario.getDiaICodigo()) &&
                                    currentHorario.getGruhorTHoraInicio().equals(horario.getGruhorTHoraInicio()) &&
                                    currentHorario.getGruhorTHoraFinal().equals(horario.getGruhorTHoraFinal()) &&
                                    currentHorario.getDocICodigo().equals(horario.getDocICodigo())) {
                        horariosEq.add(horario);
                    }
                }
            }
        }

        return horariosEq;
    }
}
