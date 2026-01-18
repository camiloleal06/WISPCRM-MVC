package org.wispcrm.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.wispcrm.interfaces.PlanInterface;
import org.wispcrm.modelo.planes.Plan;

@Controller
public class PlanController {

    @Autowired
    private PlanInterface planDao;
    private static final String LISTAR_PLAN = "plan/listaPlan";
    private static final String VER_FORM_PLAN = "plan/formPlan";

    @RequestMapping(value = "/listarplanes")
    public String listarfactura(Model modelo) {
        List<Plan> plan = planDao.findAll();
        modelo.addAttribute("listaplan", plan);
        return LISTAR_PLAN;
    }

    @GetMapping("/formplan")
    public String crear(Model modelo) {
        Plan plan = new Plan();
        modelo.addAttribute("plan", plan);
        modelo.addAttribute("titulo", "Nuevo Plan");
        return VER_FORM_PLAN;
    }

    @PostMapping("/saveplan")
    public String savePlan(Plan plan, Model modelo) {
        planDao.save(plan);
        List<Plan> listaplan = planDao.findAll();
        modelo.addAttribute("listaplan", listaplan);
        return "redirect:/listar";

    }
}
