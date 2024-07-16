# Makefile
# see https://www.gnu.org/software/make/manual/make.html

NAME := social-network
IMAGE_NAME := diegoalveslv/$(NAME)
IMAGE_TAG := latest

.PHONY: help
.DEFAULT_GOAL := help

# GENERAL

help :
	@echo ""
	@echo "*** $(NAME) Makefile help ***"
	@echo ""
	@echo "Targets list:"
	@grep -E '^[a-zA-Z_-]+ :.*?## .*$$' $(MAKEFILE_LIST) | sort -k 1,1 | awk 'BEGIN {FS = ":.*?## "}; {printf "\t\033[36m%-30s\033[0m %s\n", $$1, $$2}'
	@echo ""

print-variables : ## Print variables values
	@echo "MAKE: $(MAKE)"
	@echo "MAKEFILES: $(MAKEFILES)"
	@echo "MAKEFILE_LIST: $(MAKEFILE_LIST)"
	@echo "- - - "
	@echo "NAME: $(NAME)"
	@echo "IMAGE_NAME: $(IMAGE_NAME)"
	@echo "IMAGE_TAG: $(IMAGE_TAG)"

springdoc-gen : ## Run application and generate OpenApi spec with springdoc
	./gradlew generateOpenApiDocs
