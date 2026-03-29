package crm.branch;

import crm.common.response.ApiResponse;
import crm.common.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/branches")
@RequiredArgsConstructor
public class BranchController {

    private final BranchRepository branchRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Branch>>> getAll() {
        return ResponseEntity.ok(ApiResponse.ok(branchRepository.findAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Branch>> getById(@PathVariable Long id) {
        var branch = branchRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Branch not found"));
        return ResponseEntity.ok(ApiResponse.ok(branch));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('USER_MANAGE')")
    public ResponseEntity<ApiResponse<Branch>> create(@RequestBody Branch branch) {
        return ResponseEntity.ok(ApiResponse.ok(branchRepository.save(branch)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('USER_MANAGE')")
    public ResponseEntity<ApiResponse<Branch>> update(
            @PathVariable Long id, @RequestBody Branch updated) {
        var branch = branchRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Branch not found"));
        branch.setName(updated.getName());
        branch.setAddress(updated.getAddress());
        branch.setPhone(updated.getPhone());
        return ResponseEntity.ok(ApiResponse.ok(branchRepository.save(branch)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('USER_MANAGE')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        branchRepository.deleteById(id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}